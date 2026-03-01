package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.core.common.DateProvider
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import com.kazvoeten.omadketonics.domain.repository.WeekPlanRepository
import com.kazvoeten.omadketonics.model.MealHistoryEntry
import com.kazvoeten.omadketonics.model.Recipe
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class LogMealUseCase @Inject constructor(
    private val weekPlanRepository: WeekPlanRepository,
    private val trackingRepository: TrackingRepository,
    private val queueMealNutritionWriteUseCase: QueueMealNutritionWriteUseCase,
    private val dateProvider: DateProvider,
) {
    suspend fun setRecipeEaten(recipe: Recipe, eaten: Boolean) {
        weekPlanRepository.setMealEatenInCurrentWeek(recipe.id, eaten)
        val today = dateProvider.today()
        if (eaten) {
            trackingRepository.removeMealHistoryForDate(today)
            val entry = MealHistoryEntry(
                date = today,
                mealId = recipe.id,
                name = recipe.name,
                calories = recipe.calories,
                protein = recipe.protein,
                carbs = recipe.carbs,
                fat = recipe.fat,
            )
            trackingRepository.upsertMealHistory(
                MealHistoryEntry(
                    date = entry.date,
                    mealId = entry.mealId,
                    name = entry.name,
                    calories = entry.calories,
                    protein = entry.protein,
                    carbs = entry.carbs,
                    fat = entry.fat,
                ),
            )
            queueMealNutritionWriteUseCase.upsert(entry)
            return
        }

        val todayEntry = trackingRepository
            .observeMealHistory()
            .first()
            .firstOrNull { it.date == today }
        if (todayEntry?.mealId == recipe.id) {
            trackingRepository.removeMealHistoryForDate(today)
            queueMealNutritionWriteUseCase.delete(today)
        }
    }

    suspend fun markRecipeEaten(recipe: Recipe) {
        setRecipeEaten(recipe, eaten = true)
    }

    suspend fun logCheatMeal(
        name: String,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
    ): String? {
        if (calories <= 0) return "Calories must be greater than 0."
        if (protein < 0 || carbs < 0 || fat < 0) return "Macros cannot be negative."

        val title = name.trim().ifBlank { "Cheat Meal" }
        val today = dateProvider.today()
        trackingRepository.removeMealHistoryForDate(today)
        val entry = MealHistoryEntry(
            date = today,
            mealId = "cheat-$today",
            name = "CHEAT: $title",
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
        )
        trackingRepository.upsertMealHistory(entry)
        queueMealNutritionWriteUseCase.upsert(entry)
        return null
    }
}
