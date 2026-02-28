package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.core.common.DateProvider
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import com.kazvoeten.omadketonics.model.DayTrend
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetLast7DaysTrendUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository,
    private val dateProvider: DateProvider,
) {
    operator fun invoke(): Flow<List<DayTrend>> {
        val formatter = DateTimeFormatter.ofPattern("EEE", Locale.US)
        return combine(
            trackingRepository.observeMealHistory(),
            trackingRepository.observeWeights(),
        ) { mealHistory, weights ->
            val historyByDate = mealHistory.associateBy { it.date }
            (0..6).map { offset ->
                val day = dateProvider.today().minusDays((6 - offset).toLong())
                val entry = historyByDate[day]
                DayTrend(
                    date = day,
                    dayLabel = day.format(formatter),
                    weight = weights[day],
                    calories = entry?.calories ?: 0,
                )
            }
        }
    }
}
