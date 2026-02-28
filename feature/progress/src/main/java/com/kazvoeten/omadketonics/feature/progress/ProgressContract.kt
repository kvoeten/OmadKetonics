package com.kazvoeten.omadketonics.feature.progress

import com.kazvoeten.omadketonics.model.ChartType
import com.kazvoeten.omadketonics.model.DayTrend
import com.kazvoeten.omadketonics.model.DailyMood
import com.kazvoeten.omadketonics.model.MealHistoryEntry
import java.time.LocalDate

data class ProgressUiState(
    val isLoading: Boolean = true,
    val chartType: ChartType = ChartType.Weight,
    val trend: List<DayTrend> = emptyList(),
    val mealHistory: List<MealHistoryEntry> = emptyList(),
    val weightMap: Map<LocalDate, Float> = emptyMap(),
    val moodMap: Map<LocalDate, DailyMood> = emptyMap(),
)

sealed interface ProgressUiEvent {
    data class SaveWeight(val input: String) : ProgressUiEvent
    data class SetChartType(val chartType: ChartType) : ProgressUiEvent
}

sealed interface ProgressEffect {
    data class Message(val value: String) : ProgressEffect
}
