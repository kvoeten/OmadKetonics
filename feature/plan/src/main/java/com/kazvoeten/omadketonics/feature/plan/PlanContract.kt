package com.kazvoeten.omadketonics.feature.plan

import com.kazvoeten.omadketonics.model.DailyMood
import com.kazvoeten.omadketonics.model.HealthAvailability
import com.kazvoeten.omadketonics.model.Ingredient
import com.kazvoeten.omadketonics.model.MacroAverages
import com.kazvoeten.omadketonics.model.ProgressDeepLinkMetric

data class PlanMealItemUi(
    val recipeId: String,
    val recipeIcon: String,
    val recipeImageUri: String?,
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val isEaten: Boolean,
    val rating: Int,
)

data class PlanUiState(
    val isLoading: Boolean = true,
    val weekTitle: String = "",
    val weekSubtitle: String = "",
    val isViewingCurrentWeek: Boolean = true,
    val averages: MacroAverages = MacroAverages(0, 0, 0, 0),
    val todayMood: DailyMood? = null,
    val meals: List<PlanMealItemUi> = emptyList(),
    val healthAvailability: HealthAvailability = HealthAvailability.Unavailable,
    val healthConnected: Boolean = false,
    val healthPendingOutbox: Int = 0,
    val quickSleepMinutes: Int = 0,
    val quickSleepRecoveryScore: Int = 0,
    val quickActivityCalories: Int = 0,
    val quickActivityMinutes: Int = 0,
    val showWeightDialog: Boolean = false,
    val weightInput: String = "",
)

sealed interface PlanUiEvent {
    data class SetMood(val mood: DailyMood?) : PlanUiEvent
    data class SetMealEaten(
        val recipeId: String,
        val eaten: Boolean,
        val capturedImageUri: String? = null,
        val rating: Int? = null,
    ) : PlanUiEvent

    data class LogCheatMeal(
        val name: String,
        val calories: Int,
        val protein: Int,
        val carbs: Int,
        val fat: Int,
    ) : PlanUiEvent

    data object OpenSleepInsights : PlanUiEvent
    data object OpenActivityInsights : PlanUiEvent
    data object OpenActivityLogger : PlanUiEvent
    data object ShowWeightDialog : PlanUiEvent
    data object DismissWeightDialog : PlanUiEvent
    data class UpdateWeightInput(val value: String) : PlanUiEvent
    data object SaveWeight : PlanUiEvent

    data object GenerateWeek : PlanUiEvent
}

sealed interface PlanEffect {
    data class Message(val value: String) : PlanEffect
    data class OpenProgress(
        val metric: ProgressDeepLinkMetric,
        val openActivityLogger: Boolean = false,
    ) : PlanEffect
}
