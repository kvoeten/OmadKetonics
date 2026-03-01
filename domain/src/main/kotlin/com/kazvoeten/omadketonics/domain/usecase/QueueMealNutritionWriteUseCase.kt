package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.domain.repository.HealthRepository
import com.kazvoeten.omadketonics.model.MealHistoryEntry
import java.time.LocalDate
import javax.inject.Inject

class QueueMealNutritionWriteUseCase @Inject constructor(
    private val healthRepository: HealthRepository,
) {
    suspend fun upsert(entry: MealHistoryEntry) {
        healthRepository.queueNutritionUpsert(
            date = entry.date,
            mealName = entry.name,
            calories = entry.calories,
            protein = entry.protein,
            carbs = entry.carbs,
            fat = entry.fat,
        )
    }

    suspend fun delete(date: LocalDate) {
        healthRepository.queueNutritionDelete(date)
    }
}
