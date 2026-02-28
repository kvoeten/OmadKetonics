package com.kazvoeten.omadketonics.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import com.kazvoeten.omadketonics.domain.repository.UserPreferencesRepository
import com.kazvoeten.omadketonics.domain.usecase.GetLast7DaysTrendUseCase
import com.kazvoeten.omadketonics.domain.usecase.LogWeightUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getLast7DaysTrendUseCase: GetLast7DaysTrendUseCase,
    private val trackingRepository: TrackingRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val logWeightUseCase: LogWeightUseCase,
) : ViewModel() {
    private val effectEmitter = MutableSharedFlow<ProgressEffect>(extraBufferCapacity = 8)
    val effects = effectEmitter

    val state = combine(
        getLast7DaysTrendUseCase(),
        trackingRepository.observeMealHistory(),
        trackingRepository.observeWeights(),
        trackingRepository.observeMoods(),
        userPreferencesRepository.chartType,
    ) { trend, mealHistory, weights, moods, chartType ->
        ProgressUiState(
            isLoading = false,
            chartType = chartType,
            trend = trend,
            mealHistory = mealHistory,
            weightMap = weights,
            moodMap = moods,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState(),
    )

    fun onEvent(event: ProgressUiEvent) {
        when (event) {
            is ProgressUiEvent.SetChartType -> {
                viewModelScope.launch {
                    userPreferencesRepository.setChartType(event.chartType)
                }
            }

            is ProgressUiEvent.SaveWeight -> {
                viewModelScope.launch {
                    if (logWeightUseCase(event.input)) {
                        effectEmitter.emit(ProgressEffect.Message("Weight saved"))
                    } else {
                        effectEmitter.emit(ProgressEffect.Message("Invalid weight"))
                    }
                }
            }
        }
    }
}
