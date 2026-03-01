package com.kazvoeten.omadketonics.feature.plan

import com.kazvoeten.omadketonics.model.DailyMood
import com.kazvoeten.omadketonics.model.Ingredient
import com.kazvoeten.omadketonics.model.MacroAverages

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

    data object GenerateWeek : PlanUiEvent
}

sealed interface PlanEffect {
    data class Message(val value: String) : PlanEffect
}
