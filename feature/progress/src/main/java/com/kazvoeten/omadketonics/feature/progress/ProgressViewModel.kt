package com.kazvoeten.omadketonics.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazvoeten.omadketonics.core.common.DateProvider
import com.kazvoeten.omadketonics.domain.repository.HealthRepository
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import com.kazvoeten.omadketonics.domain.usecase.BackfillNutritionToHealthUseCase
import com.kazvoeten.omadketonics.domain.usecase.BuildProgressInsightsUseCase
import com.kazvoeten.omadketonics.domain.usecase.GetLast7DaysTrendUseCase
import com.kazvoeten.omadketonics.domain.usecase.LogManualActivityUseCase
import com.kazvoeten.omadketonics.domain.usecase.SyncHealthDataUseCase
import com.kazvoeten.omadketonics.model.DayTrend
import com.kazvoeten.omadketonics.model.InsightRange
import com.kazvoeten.omadketonics.model.ManualActivityInput
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val trackingRepository: TrackingRepository,
    private val getLast7DaysTrendUseCase: GetLast7DaysTrendUseCase,
    private val backfillNutritionToHealthUseCase: BackfillNutritionToHealthUseCase,
    private val buildProgressInsightsUseCase: BuildProgressInsightsUseCase,
    private val logManualActivityUseCase: LogManualActivityUseCase,
    private val syncHealthDataUseCase: SyncHealthDataUseCase,
    private val dateProvider: DateProvider,
) : ViewModel() {
    private val effectEmitter = MutableSharedFlow<ProgressEffect>(extraBufferCapacity = 8)
    val effects = effectEmitter

    private val today = dateProvider.today()
    private val weekStart = today.minusDays(6)
    private val monthStart = today.minusDays(27)
    private val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.US)

    private val ephemeralState = MutableStateFlow(
        ProgressEphemeralState(
            selectedMetric = null,
            selectedRange = InsightRange.Week,
            showActivityLogger = false,
            activityForm = ActivityLogFormState(),
        ),
    )

    private val monthlyTrend = combine(
        trackingRepository.observeMealHistoryBetween(monthStart, today),
        trackingRepository.observeWeights(),
    ) { mealHistory, weights ->
        val byDate = mealHistory.associateBy { it.date }
        (0..27).map { idx ->
            val day = monthStart.plusDays(idx.toLong())
            DayTrend(
                date = day,
                dayLabel = day.format(dayFormatter),
                weight = weights[day],
                calories = byDate[day]?.calories ?: 0,
            )
        }
    }

    private val progressPrimary = combine(
        healthRepository.connectionState,
        getLast7DaysTrendUseCase(),
        monthlyTrend,
    ) { connectionState, weeklyTrend, monthTrend ->
        ProgressPrimaryData(
            connectionState = connectionState,
            weeklyTrend = weeklyTrend,
            monthTrend = monthTrend,
        )
    }

    private val progressSecondary = combine(
        trackingRepository.observeMoods(),
        healthRepository.observeDailySummaries(weekStart, today),
        healthRepository.observeDailySummaries(monthStart, today),
        healthRepository.observeManualActivityLogs(monthStart, today),
    ) { moods, weeklySummaries, monthlySummaries, activityLogs ->
        ProgressSecondaryData(
            moods = moods,
            weeklySummaries = weeklySummaries,
            monthlySummaries = monthlySummaries,
            activityLogs = activityLogs,
        )
    }

    private val progressData = combine(progressPrimary, progressSecondary) { primary, secondary ->
        ProgressData(
            connectionState = primary.connectionState,
            weeklyTrend = primary.weeklyTrend,
            monthTrend = primary.monthTrend,
            moods = secondary.moods,
            weeklySummaries = secondary.weeklySummaries,
            monthlySummaries = secondary.monthlySummaries,
            activityLogs = secondary.activityLogs,
        )
    }

    val state = combine(progressData, ephemeralState) { data, ephemeral ->
        ProgressUiState(
            isLoading = false,
            connectionState = data.connectionState,
            weeklyTrend = data.weeklyTrend,
            weeklyHealthSummaries = data.weeklySummaries,
            monthlyHealthSummaries = data.monthlySummaries,
            manualActivityLogs = data.activityLogs,
            weeklyInsights = buildProgressInsightsUseCase(
                summaries = data.weeklySummaries,
                trends = data.weeklyTrend,
                moodMap = data.moods,
                range = InsightRange.Week,
            ),
            monthlyInsights = buildProgressInsightsUseCase(
                summaries = data.monthlySummaries,
                trends = data.monthTrend,
                moodMap = data.moods,
                range = InsightRange.Month,
            ),
            selectedMetric = ephemeral.selectedMetric,
            selectedRange = ephemeral.selectedRange,
            showActivityLogger = ephemeral.showActivityLogger,
            activityForm = ephemeral.activityForm,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState(),
    )

    fun requiredPermissions(): Set<String> = healthRepository.requiredPermissions()

    fun onEvent(event: ProgressUiEvent) {
        when (event) {
            ProgressUiEvent.TabOpened -> {
                viewModelScope.launch {
                    runCatching { syncHealthDataUseCase(daysBack = 90) }
                }
            }

            ProgressUiEvent.RefreshSync -> {
                viewModelScope.launch {
                    runCatching {
                        syncHealthDataUseCase(daysBack = 90)
                        effectEmitter.emit(ProgressEffect.Message("Health data synced"))
                    }.onFailure {
                        effectEmitter.emit(ProgressEffect.Message(it.message ?: "Health sync failed"))
                    }
                }
            }

            ProgressUiEvent.ConnectHealth -> {
                viewModelScope.launch {
                    effectEmitter.emit(ProgressEffect.RequestHealthPermissions(healthRepository.requiredPermissions()))
                }
            }

            is ProgressUiEvent.HealthPermissionsResult -> {
                viewModelScope.launch {
                    healthRepository.updateGrantedPermissions(event.grantedPermissions)
                    if (healthRepository.connectionState.first().hasPermissions) {
                        runCatching { backfillNutritionToHealthUseCase(daysBack = 90) }
                        runCatching { syncHealthDataUseCase(daysBack = 90) }
                        effectEmitter.emit(ProgressEffect.Message("Health Connect linked"))
                    } else {
                        effectEmitter.emit(ProgressEffect.Message("Health permissions still missing"))
                    }
                }
            }

            is ProgressUiEvent.OpenMetric -> {
                ephemeralState.update { it.copy(selectedMetric = event.metric, selectedRange = InsightRange.Week) }
            }

            ProgressUiEvent.CloseMetric -> {
                ephemeralState.update { it.copy(selectedMetric = null) }
            }

            is ProgressUiEvent.SelectRange -> {
                ephemeralState.update { it.copy(selectedRange = event.range) }
            }

            ProgressUiEvent.ShowActivityLogger -> {
                ephemeralState.update { it.copy(showActivityLogger = true) }
            }

            ProgressUiEvent.DismissActivityLogger -> {
                ephemeralState.update { it.copy(showActivityLogger = false) }
            }

            is ProgressUiEvent.UpdateActivityType -> {
                ephemeralState.update { it.copy(activityForm = it.activityForm.copy(activityType = event.value)) }
            }

            is ProgressUiEvent.UpdateActivityDuration -> {
                ephemeralState.update { it.copy(activityForm = it.activityForm.copy(durationMinutes = event.value)) }
            }

            is ProgressUiEvent.UpdateActivityExertion -> {
                ephemeralState.update { it.copy(activityForm = it.activityForm.copy(exertion = event.value)) }
            }

            is ProgressUiEvent.UpdateActivityCaloriesOverride -> {
                ephemeralState.update { it.copy(activityForm = it.activityForm.copy(caloriesOverride = event.value)) }
            }

            is ProgressUiEvent.UpdateActivityNotes -> {
                ephemeralState.update { it.copy(activityForm = it.activityForm.copy(notes = event.value)) }
            }

            ProgressUiEvent.SaveActivityLog -> {
                saveActivityLog()
            }
        }
    }

    private fun saveActivityLog() {
        viewModelScope.launch {
            val form = state.value.activityForm
            val duration = form.durationMinutes.toIntOrNull()
            if (duration == null || duration <= 0) {
                effectEmitter.emit(ProgressEffect.Message("Duration must be a positive number"))
                return@launch
            }

            val exertion = form.exertion.toIntOrNull()
            if (exertion == null || exertion !in 1..10) {
                effectEmitter.emit(ProgressEffect.Message("Exertion must be between 1 and 10"))
                return@launch
            }

            val override = form.caloriesOverride.toIntOrNull()
            if (form.caloriesOverride.isNotBlank() && override == null) {
                effectEmitter.emit(ProgressEffect.Message("Calorie override must be numeric"))
                return@launch
            }

            val end = System.currentTimeMillis()
            val start = end - (duration * 60_000L)

            val result = logManualActivityUseCase(
                ManualActivityInput(
                    startTimeEpochMillis = start,
                    endTimeEpochMillis = end,
                    activityType = form.activityType,
                    exertion = exertion,
                    caloriesOverride = override,
                    notes = form.notes.ifBlank { null },
                ),
            )

            if (!result.success) {
                effectEmitter.emit(ProgressEffect.Message(result.error ?: "Unable to log activity"))
                return@launch
            }

            ephemeralState.update {
                it.copy(
                    showActivityLogger = false,
                    activityForm = ActivityLogFormState(),
                )
            }
            effectEmitter.emit(ProgressEffect.Message("Activity logged: ${result.calories} kcal"))
            runCatching { syncHealthDataUseCase(daysBack = 90) }
        }
    }

    private data class ProgressEphemeralState(
        val selectedMetric: ProgressMetric?,
        val selectedRange: InsightRange,
        val showActivityLogger: Boolean,
        val activityForm: ActivityLogFormState,
    )

    private data class ProgressData(
        val connectionState: com.kazvoeten.omadketonics.model.HealthConnectionState,
        val weeklyTrend: List<DayTrend>,
        val monthTrend: List<DayTrend>,
        val moods: Map<java.time.LocalDate, com.kazvoeten.omadketonics.model.DailyMood>,
        val weeklySummaries: List<com.kazvoeten.omadketonics.model.DailyHealthSummary>,
        val monthlySummaries: List<com.kazvoeten.omadketonics.model.DailyHealthSummary>,
        val activityLogs: List<com.kazvoeten.omadketonics.model.ManualActivityLog>,
    )

    private data class ProgressPrimaryData(
        val connectionState: com.kazvoeten.omadketonics.model.HealthConnectionState,
        val weeklyTrend: List<DayTrend>,
        val monthTrend: List<DayTrend>,
    )

    private data class ProgressSecondaryData(
        val moods: Map<java.time.LocalDate, com.kazvoeten.omadketonics.model.DailyMood>,
        val weeklySummaries: List<com.kazvoeten.omadketonics.model.DailyHealthSummary>,
        val monthlySummaries: List<com.kazvoeten.omadketonics.model.DailyHealthSummary>,
        val activityLogs: List<com.kazvoeten.omadketonics.model.ManualActivityLog>,
    )
}
