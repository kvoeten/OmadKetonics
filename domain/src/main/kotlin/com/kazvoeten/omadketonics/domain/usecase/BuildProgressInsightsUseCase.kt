package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.core.common.DateProvider
import com.kazvoeten.omadketonics.model.DailyHealthSummary
import com.kazvoeten.omadketonics.model.DailyMood
import com.kazvoeten.omadketonics.model.DayTrend
import com.kazvoeten.omadketonics.model.InsightRange
import com.kazvoeten.omadketonics.model.ProgressInsights
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

class BuildProgressInsightsUseCase @Inject constructor(
    private val dateProvider: DateProvider,
) {
    operator fun invoke(
        summaries: List<DailyHealthSummary>,
        trends: List<DayTrend>,
        moodMap: Map<LocalDate, DailyMood>,
        range: InsightRange,
    ): ProgressInsights {
        val endDate = dateProvider.today()
        val days = when (range) {
            InsightRange.Day -> 1L
            InsightRange.Week -> 7L
            InsightRange.Month -> 28L
        }
        val startDate = endDate.minusDays(days - 1)

        val scopedSummaries = summaries.filter { it.date in startDate..endDate }.sortedBy { it.date }
        val scopedTrends = trends.filter { it.date in startDate..endDate }

        val avgSleep = scopedSummaries.map { it.sleep.totalSleepMinutes }.average().let {
            if (it.isNaN()) 0 else it.roundToInt()
        }
        val sleepGoalMetDays = scopedSummaries.count { it.sleep.totalSleepMinutes >= 450 }
        val consistencyPct = if (scopedSummaries.isEmpty()) 0 else ((sleepGoalMetDays.toFloat() / scopedSummaries.size) * 100f).roundToInt()
        val deepRatio = scopedSummaries.map {
            if (it.sleep.totalSleepMinutes <= 0) 0f else it.sleep.deepSleepMinutes.toFloat() / it.sleep.totalSleepMinutes.toFloat()
        }.average().let { if (it.isNaN()) 0f else it.toFloat() }
        val recoveryScore = (
            ((avgSleep / 450f).coerceIn(0f, 1.4f) * 55f) +
                (consistencyPct.coerceIn(0, 100) * 0.3f) +
                (deepRatio.coerceIn(0f, 0.5f) * 100f * 0.15f)
            ).roundToInt().coerceIn(0, 100)

        val exerciseMinutes = scopedSummaries.sumOf { it.activity.exerciseMinutes }
        val activeCalories = scopedSummaries.sumOf { it.activity.activeCalories }

        val half = (scopedSummaries.size / 2).coerceAtLeast(1)
        val firstHalfAvg = scopedSummaries.take(half).map { it.activity.activeCalories }.average().let { if (it.isNaN()) 0.0 else it }
        val secondHalfAvg = scopedSummaries.takeLast(half).map { it.activity.activeCalories }.average().let { if (it.isNaN()) 0.0 else it }
        val activityDeltaPct = if (firstHalfAvg <= 0.0) 0 else (((secondHalfAvg - firstHalfAvg) / firstHalfAvg) * 100.0).roundToInt()

        val sleepMoodPairs = scopedSummaries.mapNotNull { summary ->
            moodMap[summary.date]?.let { mood ->
                val moodScore = when (mood) {
                    DailyMood.Terrible -> 1
                    DailyMood.Low -> 2
                    DailyMood.Okay -> 3
                    DailyMood.Good -> 4
                    DailyMood.Great -> 5
                }
                summary.sleep.totalSleepMinutes to moodScore
            }
        }
        val moodCorrelationPct = correlationPct(sleepMoodPairs)

        val calorieAvg = scopedTrends.map { it.calories }.average().let { if (it.isNaN()) 0 else it.roundToInt() }
        val caloriePeak = scopedTrends.maxOfOrNull { it.calories } ?: 0

        val sleepNarrative = when {
            recoveryScore >= 80 -> "Sleep consistency is supporting strong recovery and energy output."
            recoveryScore >= 60 -> "Sleep is stable but still has room to improve nightly consistency."
            else -> "Sleep debt is likely suppressing recovery; prioritize 7.5+ hours tonight."
        }

        val activityNarrative = when {
            activityDeltaPct >= 10 -> "Activity load is trending up. Keep hydration and protein high for recovery."
            activityDeltaPct <= -10 -> "Activity load dropped recently. A short walk or Zone 2 block can restore momentum."
            else -> "Activity load is steady and sustainable across the selected range."
        }

        val caloriesNarrative = when {
            calorieAvg == 0 -> "No calorie entries were found for this range yet."
            abs(caloriePeak - calorieAvg) <= 250 -> "Calorie intake is highly consistent with your target trajectory."
            else -> "Calorie swings are elevated. Tighter meal timing may improve adherence."
        }

        return ProgressInsights(
            recoveryScore = recoveryScore,
            sleepGoalMetDays = sleepGoalMetDays,
            averageSleepMinutes = avgSleep,
            weeklyExerciseMinutes = exerciseMinutes,
            weeklyActiveCalories = activeCalories,
            activityIntensityDeltaPct = activityDeltaPct,
            moodCorrelationPct = moodCorrelationPct,
            calorieAverage = calorieAvg,
            caloriePeak = caloriePeak,
            narrativeSleep = sleepNarrative,
            narrativeActivity = activityNarrative,
            narrativeCalories = caloriesNarrative,
        )
    }

    private fun correlationPct(pairs: List<Pair<Int, Int>>): Int {
        if (pairs.size < 2) return 0
        val xs = pairs.map { it.first.toDouble() }
        val ys = pairs.map { it.second.toDouble() }
        val xMean = xs.average()
        val yMean = ys.average()
        var num = 0.0
        var denX = 0.0
        var denY = 0.0
        for (i in xs.indices) {
            val dx = xs[i] - xMean
            val dy = ys[i] - yMean
            num += dx * dy
            denX += dx * dx
            denY += dy * dy
        }
        if (denX == 0.0 || denY == 0.0) return 0
        val corr = num / kotlin.math.sqrt(denX * denY)
        return (corr * 100.0).roundToInt().coerceIn(-100, 100)
    }
}
