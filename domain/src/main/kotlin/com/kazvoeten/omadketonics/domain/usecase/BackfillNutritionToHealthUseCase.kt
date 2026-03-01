package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.core.common.DateProvider
import com.kazvoeten.omadketonics.domain.repository.HealthRepository
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class BackfillNutritionToHealthUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository,
    private val healthRepository: HealthRepository,
    private val dateProvider: DateProvider,
) {
    suspend operator fun invoke(daysBack: Long = 90L) {
        val endDate = dateProvider.today()
        val startDate = endDate.minusDays((daysBack - 1).coerceAtLeast(0L))
        val entries = trackingRepository.observeMealHistoryBetween(startDate, endDate).first()
        entries.forEach { entry ->
            healthRepository.queueNutritionUpsert(
                date = entry.date,
                mealName = entry.name,
                calories = entry.calories,
                protein = entry.protein,
                carbs = entry.carbs,
                fat = entry.fat,
            )
        }
    }
}
