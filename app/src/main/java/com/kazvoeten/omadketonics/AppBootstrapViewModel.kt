package com.kazvoeten.omadketonics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazvoeten.omadketonics.core.common.DateProvider
import com.kazvoeten.omadketonics.domain.policy.WeekMealSelectionPolicy
import com.kazvoeten.omadketonics.domain.repository.RecipeRepository
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import com.kazvoeten.omadketonics.domain.repository.WeekPlanRepository
import com.kazvoeten.omadketonics.domain.usecase.SyncHealthDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AppBootstrapViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val trackingRepository: TrackingRepository,
    private val weekPlanRepository: WeekPlanRepository,
    private val weekMealSelectionPolicy: WeekMealSelectionPolicy,
    private val syncHealthDataUseCase: SyncHealthDataUseCase,
    private val dateProvider: DateProvider,
) : ViewModel() {
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    init {
        viewModelScope.launch {
            recipeRepository.ensureSeeded()
            val recipes = recipeRepository.getRecipes()
            val ratings = trackingRepository.getRatings()
            val mealIds = weekMealSelectionPolicy.selectMealIds(recipes, ratings)
            weekPlanRepository.ensureCurrentWeekExists(
                mealIds = mealIds,
                date = dateProvider.sundayStart(dateProvider.today()),
            )
            runCatching { syncHealthDataUseCase(daysBack = 90) }
            _ready.value = true
        }
    }
}
