package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.core.common.DateProvider
import com.kazvoeten.omadketonics.domain.repository.HealthRepository
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import com.kazvoeten.omadketonics.model.ManualActivityInput
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first

data class LogManualActivityResult(
    val success: Boolean,
    val calories: Int = 0,
    val error: String? = null,
)

class LogManualActivityUseCase @Inject constructor(
    private val healthRepository: HealthRepository,
    private val trackingRepository: TrackingRepository,
    private val dateProvider: DateProvider,
) {
    suspend operator fun invoke(input: ManualActivityInput): LogManualActivityResult {
        val start = input.startTimeEpochMillis
        val end = input.endTimeEpochMillis
        if (end <= start) {
            return LogManualActivityResult(success = false, error = "End time must be after start time.")
        }
        if (input.exertion !in 1..10) {
            return LogManualActivityResult(success = false, error = "Exertion must be between 1 and 10.")
        }

        val durationMinutes = ((end - start) / 60_000L).toInt().coerceAtLeast(1)
        val todayWeight = trackingRepository.observeWeights().first()[dateProvider.today()]
        val weightKg = (todayWeight ?: 80f).coerceAtLeast(35f)

        val estimatedCalories = input.caloriesOverride ?: estimateCalories(
            activityType = input.activityType,
            exertion = input.exertion,
            durationMinutes = durationMinutes,
            weightKg = weightKg,
        )

        val clientRecordId = "activity-${input.startTimeEpochMillis}-${normalizeType(input.activityType)}"
        healthRepository.queueManualActivity(
            input = input,
            estimatedCalories = estimatedCalories,
            clientRecordId = clientRecordId,
            source = "app_manual",
        )

        return LogManualActivityResult(success = true, calories = estimatedCalories)
    }

    private fun estimateCalories(
        activityType: String,
        exertion: Int,
        durationMinutes: Int,
        weightKg: Float,
    ): Int {
        val baseMet = when {
            activityType.contains("run", ignoreCase = true) -> 9.5f
            activityType.contains("hiit", ignoreCase = true) -> 10.0f
            activityType.contains("walk", ignoreCase = true) -> 3.5f
            activityType.contains("cycle", ignoreCase = true) -> 7.0f
            activityType.contains("lift", ignoreCase = true) -> 6.0f
            else -> 4.5f
        }
        val intensityMultiplier = 0.6f + (exertion.coerceIn(1, 10) * 0.1f)
        val met = baseMet * intensityMultiplier
        val caloriesPerMinute = met * 3.5f * weightKg / 200f
        return (caloriesPerMinute * durationMinutes).roundToInt().coerceAtLeast(1)
    }

    private fun normalizeType(raw: String): String {
        return raw.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "-").trim('-')
    }
}
