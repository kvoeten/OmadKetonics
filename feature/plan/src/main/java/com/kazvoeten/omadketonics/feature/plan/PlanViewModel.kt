package com.kazvoeten.omadketonics.feature.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazvoeten.omadketonics.core.common.DateProvider
import com.kazvoeten.omadketonics.domain.repository.HealthRepository
import com.kazvoeten.omadketonics.domain.repository.RecipeRepository
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import com.kazvoeten.omadketonics.domain.usecase.GenerateWeekPlanUseCase
import com.kazvoeten.omadketonics.domain.usecase.GetDisplayedWeekUseCase
import com.kazvoeten.omadketonics.domain.usecase.GetWeeklyAveragesUseCase
import com.kazvoeten.omadketonics.domain.usecase.LogMealUseCase
import com.kazvoeten.omadketonics.domain.usecase.LogWeightUseCase
import com.kazvoeten.omadketonics.domain.usecase.SetMoodUseCase
import com.kazvoeten.omadketonics.model.DailyHealthSummary
import com.kazvoeten.omadketonics.model.DailyMood
import com.kazvoeten.omadketonics.model.HealthConnectionState
import com.kazvoeten.omadketonics.model.ProgressDeepLinkMetric
import com.kazvoeten.omadketonics.model.Recipe
import com.kazvoeten.omadketonics.model.WeekSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val getDisplayedWeekUseCase: GetDisplayedWeekUseCase,
    private val recipeRepository: RecipeRepository,
    private val trackingRepository: TrackingRepository,
    private val healthRepository: HealthRepository,
    private val getWeeklyAveragesUseCase: GetWeeklyAveragesUseCase,
    private val generateWeekPlanUseCase: GenerateWeekPlanUseCase,
    private val logMealUseCase: LogMealUseCase,
    private val setMoodUseCase: SetMoodUseCase,
    private val logWeightUseCase: LogWeightUseCase,
    private val dateProvider: DateProvider,
) : ViewModel() {
    private val effectEmitter = MutableSharedFlow<PlanEffect>(extraBufferCapacity = 8)
    val effects = effectEmitter

    private val today = dateProvider.today()
    private val yesterday = today.minusDays(1)

    private val ephemeralState = MutableStateFlow(PlanEphemeralState())

    private val planPrimary = combine(
        getDisplayedWeekUseCase().filterNotNull(),
        recipeRepository.observeRecipes(),
        trackingRepository.observeRatings(),
        trackingRepository.observeMoods(),
    ) { displayedWeek, recipes, ratings, moods ->
        PlanPrimaryData(
            displayedWeek = displayedWeek,
            recipes = recipes,
            ratings = ratings,
            moods = moods,
        )
    }

    private val planHealth = combine(
        healthRepository.connectionState,
        healthRepository.observeDailySummaries(yesterday, yesterday),
        healthRepository.observeDailySummaries(today, today),
    ) { healthConnection, sleepSummary, todayHealthSummary ->
        PlanHealthData(
            healthConnection = healthConnection,
            sleepSummary = sleepSummary.firstOrNull(),
            todayHealthSummary = todayHealthSummary.firstOrNull(),
        )
    }

    private val planData = combine(planPrimary, planHealth) { primary, health ->
        PlanData(
            displayedWeek = primary.displayedWeek,
            recipes = primary.recipes,
            ratings = primary.ratings,
            moods = primary.moods,
            healthConnection = health.healthConnection,
            sleepSummary = health.sleepSummary,
            todayHealthSummary = health.todayHealthSummary,
        )
    }

    val state = combine(planData, ephemeralState) { data, ephemeral ->
        val recipeById = data.recipes.associateBy { it.id }
        val weekRecipes = data.displayedWeek.snapshot.mealIds.mapNotNull { recipeById[it] }
        val eatenSet = data.displayedWeek.snapshot.eatenMealIds.toSet()

        val meals = weekRecipes.map { recipe ->
            PlanMealItemUi(
                recipeId = recipe.id,
                recipeIcon = recipe.icon,
                recipeImageUri = recipe.imageUri,
                name = recipe.name,
                calories = recipe.calories,
                protein = recipe.protein,
                carbs = recipe.carbs,
                fat = recipe.fat,
                ingredients = recipe.ingredients,
                instructions = recipe.instructions,
                isEaten = eatenSet.contains(recipe.id),
                rating = data.ratings[recipe.id] ?: 0,
            )
        }

        val averages = getWeeklyAveragesUseCase(weekRecipes)
        val quickSleep = data.sleepSummary
        val quickTodayActivity = data.todayHealthSummary

        PlanUiState(
            isLoading = false,
            weekTitle = buildWeekTitle(data.displayedWeek.snapshot),
            weekSubtitle = buildWeekSubtitle(data.displayedWeek.snapshot, data.displayedWeek.isViewingCurrentWeek),
            isViewingCurrentWeek = data.displayedWeek.isViewingCurrentWeek,
            averages = averages,
            todayMood = data.moods[today],
            meals = meals,
            healthAvailability = data.healthConnection.availability,
            healthConnected = data.healthConnection.hasPermissions,
            healthPendingOutbox = data.healthConnection.pendingOutboxCount,
            quickSleepMinutes = quickSleep?.sleep?.totalSleepMinutes ?: 0,
            quickSleepRecoveryScore = recoveryFromSleep(quickSleep?.sleep?.totalSleepMinutes ?: 0),
            quickActivityCalories = quickTodayActivity?.activity?.activeCalories ?: 0,
            quickActivityMinutes = quickTodayActivity?.activity?.exerciseMinutes ?: 0,
            showWeightDialog = ephemeral.showWeightDialog,
            weightInput = ephemeral.weightInput,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlanUiState(),
    )

    fun onEvent(event: PlanUiEvent) {
        when (event) {
            is PlanUiEvent.SetMood -> {
                viewModelScope.launch {
                    setMoodUseCase(event.mood)
                }
            }

            is PlanUiEvent.GenerateWeek -> {
                viewModelScope.launch {
                    generateWeekPlanUseCase()
                }
            }

            is PlanUiEvent.SetMealEaten -> {
                viewModelScope.launch {
                    if (!state.value.isViewingCurrentWeek) {
                        effectEmitter.emit(PlanEffect.Message("Historical week is read-only"))
                        return@launch
                    }
                    val recipe = recipeRepository.getRecipe(event.recipeId)
                    if (recipe == null) {
                        effectEmitter.emit(PlanEffect.Message("Recipe not found"))
                        return@launch
                    }

                    if (event.eaten) {
                        if (!event.capturedImageUri.isNullOrBlank()) {
                            recipeRepository.saveRecipe(recipe.copy(imageUri = event.capturedImageUri))
                        }
                        event.rating?.let { rating ->
                            trackingRepository.setRating(recipe.id, rating)
                        }
                    }

                    logMealUseCase.setRecipeEaten(recipe, eaten = event.eaten)
                    effectEmitter.emit(
                        PlanEffect.Message(
                            if (event.eaten) "Meal completed and rated!" else "Meal marked as pending",
                        ),
                    )
                }
            }

            is PlanUiEvent.LogCheatMeal -> {
                viewModelScope.launch {
                    if (!state.value.isViewingCurrentWeek) {
                        effectEmitter.emit(PlanEffect.Message("Cheat logging is only available for the current week"))
                        return@launch
                    }
                    val error = logMealUseCase.logCheatMeal(
                        name = event.name,
                        calories = event.calories,
                        protein = event.protein,
                        carbs = event.carbs,
                        fat = event.fat,
                    )
                    if (error != null) {
                        effectEmitter.emit(PlanEffect.Message(error))
                    } else {
                        effectEmitter.emit(PlanEffect.Message("Cheat meal logged"))
                    }
                }
            }

            PlanUiEvent.OpenSleepInsights -> {
                viewModelScope.launch {
                    effectEmitter.emit(PlanEffect.OpenProgress(metric = ProgressDeepLinkMetric.SleepQuality))
                }
            }

            PlanUiEvent.OpenActivityInsights -> {
                viewModelScope.launch {
                    effectEmitter.emit(PlanEffect.OpenProgress(metric = ProgressDeepLinkMetric.ExerciseLoad))
                }
            }

            PlanUiEvent.OpenActivityLogger -> {
                viewModelScope.launch {
                    effectEmitter.emit(
                        PlanEffect.OpenProgress(
                            metric = ProgressDeepLinkMetric.ExerciseLoad,
                            openActivityLogger = true,
                        ),
                    )
                }
            }

            PlanUiEvent.ShowWeightDialog -> {
                ephemeralState.update { it.copy(showWeightDialog = true) }
            }

            PlanUiEvent.DismissWeightDialog -> {
                ephemeralState.update { it.copy(showWeightDialog = false) }
            }

            is PlanUiEvent.UpdateWeightInput -> {
                ephemeralState.update { it.copy(weightInput = event.value) }
            }

            PlanUiEvent.SaveWeight -> {
                viewModelScope.launch {
                    val success = logWeightUseCase(ephemeralState.value.weightInput)
                    if (success) {
                        ephemeralState.update { it.copy(showWeightDialog = false, weightInput = "") }
                        effectEmitter.emit(PlanEffect.Message("Weight saved"))
                    } else {
                        effectEmitter.emit(PlanEffect.Message("Invalid weight"))
                    }
                }
            }
        }
    }

    suspend fun isCurrentWeek(): Boolean {
        return getDisplayedWeekUseCase().first()?.isViewingCurrentWeek ?: false
    }

    private fun buildWeekTitle(snapshot: WeekSnapshot): String {
        val fmt = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
        return "Week Of ${snapshot.startDate.format(fmt)}"
    }

    private fun buildWeekSubtitle(snapshot: WeekSnapshot, isCurrentWeek: Boolean): String {
        val eaten = snapshot.eatenMealIds.count { snapshot.mealIds.contains(it) }
        val total = snapshot.mealIds.size
        val prefix = if (isCurrentWeek) "Current" else "Past"
        return "$prefix - $eaten / $total eaten"
    }

    private fun recoveryFromSleep(sleepMinutes: Int): Int {
        return ((sleepMinutes / 450f) * 100f).roundToInt().coerceIn(0, 100)
    }

    private data class PlanEphemeralState(
        val showWeightDialog: Boolean = false,
        val weightInput: String = "",
    )

    private data class PlanData(
        val displayedWeek: com.kazvoeten.omadketonics.domain.model.DisplayedWeek,
        val recipes: List<Recipe>,
        val ratings: Map<String, Int>,
        val moods: Map<java.time.LocalDate, DailyMood>,
        val healthConnection: HealthConnectionState,
        val sleepSummary: DailyHealthSummary?,
        val todayHealthSummary: DailyHealthSummary?,
    )

    private data class PlanPrimaryData(
        val displayedWeek: com.kazvoeten.omadketonics.domain.model.DisplayedWeek,
        val recipes: List<Recipe>,
        val ratings: Map<String, Int>,
        val moods: Map<java.time.LocalDate, DailyMood>,
    )

    private data class PlanHealthData(
        val healthConnection: HealthConnectionState,
        val sleepSummary: DailyHealthSummary?,
        val todayHealthSummary: DailyHealthSummary?,
    )
}
