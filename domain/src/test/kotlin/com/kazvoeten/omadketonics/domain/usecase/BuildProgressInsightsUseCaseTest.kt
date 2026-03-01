package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.core.common.DateProvider
import com.kazvoeten.omadketonics.model.ActivitySummary
import com.kazvoeten.omadketonics.model.DailyHealthSummary
import com.kazvoeten.omadketonics.model.DailyMood
import com.kazvoeten.omadketonics.model.DayTrend
import com.kazvoeten.omadketonics.model.InsightRange
import com.kazvoeten.omadketonics.model.SleepSummary
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildProgressInsightsUseCaseTest {
    private val useCase = BuildProgressInsightsUseCase(DateProvider())

    @Test
    fun `high sleep consistency yields strong recovery narrative`() {
        val today = LocalDate.now()
        val summaries = (0..6).map { offset ->
            val date = today.minusDays(offset.toLong())
            DailyHealthSummary(
                date = date,
                sleep = SleepSummary(
                    totalSleepMinutes = 480,
                    deepSleepMinutes = 90,
                    remSleepMinutes = 95,
                    lightSleepMinutes = 295,
                    sessionCount = 1,
                ),
                activity = ActivitySummary(
                    exerciseMinutes = 40,
                    activeCalories = 420,
                    sessionCount = 1,
                    highIntensitySessions = 0,
                    moderateIntensitySessions = 1,
                    lowIntensitySessions = 0,
                ),
            )
        }
        val trend = summaries.map {
            DayTrend(
                date = it.date,
                dayLabel = it.date.dayOfWeek.name.take(1),
                weight = 81.2f,
                calories = 1800,
            )
        }
        val moods = summaries.associate { it.date to DailyMood.Great }

        val insights = useCase(
            summaries = summaries,
            trends = trend,
            moodMap = moods,
            range = InsightRange.Week,
        )

        assertTrue(insights.recoveryScore >= 70)
        assertTrue(insights.narrativeSleep.isNotBlank())
    }

    @Test
    fun `low sleep pushes debt narrative`() {
        val today = LocalDate.now()
        val summaries = (0..6).map { offset ->
            val date = today.minusDays(offset.toLong())
            DailyHealthSummary(
                date = date,
                sleep = SleepSummary(totalSleepMinutes = 280, deepSleepMinutes = 30, remSleepMinutes = 40, lightSleepMinutes = 210, sessionCount = 1),
                activity = ActivitySummary(exerciseMinutes = 20, activeCalories = 180, sessionCount = 1),
            )
        }
        val trend = summaries.map {
            DayTrend(
                date = it.date,
                dayLabel = it.date.dayOfWeek.name.take(1),
                weight = null,
                calories = 1500,
            )
        }
        val moods = summaries.associate { it.date to DailyMood.Low }

        val insights = useCase(
            summaries = summaries,
            trends = trend,
            moodMap = moods,
            range = InsightRange.Week,
        )

        assertTrue(insights.recoveryScore < 60)
        assertTrue(insights.narrativeSleep.contains("Sleep debt"))
    }
}
