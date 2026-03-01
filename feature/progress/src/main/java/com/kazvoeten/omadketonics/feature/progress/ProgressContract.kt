package com.kazvoeten.omadketonics.feature.progress

import com.kazvoeten.omadketonics.model.DailyHealthSummary
import com.kazvoeten.omadketonics.model.DayTrend
import com.kazvoeten.omadketonics.model.HealthConnectionState
import com.kazvoeten.omadketonics.model.InsightRange
import com.kazvoeten.omadketonics.model.ManualActivityLog
import com.kazvoeten.omadketonics.model.ProgressInsights

enum class ProgressMetric {
    ActivityRings,
    ExerciseLoad,
    WeightTrend,
    CalorieConsistency,
    SleepQuality,
}

data class ActivityLogFormState(
    val activityType: String = "Walking",
    val durationMinutes: String = "45",
    val exertion: String = "6",
    val caloriesOverride: String = "",
    val notes: String = "",
)

data class ProgressUiState(
    val isLoading: Boolean = true,
    val connectionState: HealthConnectionState = HealthConnectionState(),
    val weeklyTrend: List<DayTrend> = emptyList(),
    val weeklyHealthSummaries: List<DailyHealthSummary> = emptyList(),
    val monthlyHealthSummaries: List<DailyHealthSummary> = emptyList(),
    val manualActivityLogs: List<ManualActivityLog> = emptyList(),
    val weeklyInsights: ProgressInsights? = null,
    val monthlyInsights: ProgressInsights? = null,
    val selectedMetric: ProgressMetric? = null,
    val selectedRange: InsightRange = InsightRange.Week,
    val showActivityLogger: Boolean = false,
    val activityForm: ActivityLogFormState = ActivityLogFormState(),
)

sealed interface ProgressUiEvent {
    data object TabOpened : ProgressUiEvent
    data object RefreshSync : ProgressUiEvent
    data object ConnectHealth : ProgressUiEvent
    data class HealthPermissionsResult(val grantedPermissions: Set<String>) : ProgressUiEvent

    data class OpenMetric(val metric: ProgressMetric) : ProgressUiEvent
    data object CloseMetric : ProgressUiEvent
    data class SelectRange(val range: InsightRange) : ProgressUiEvent

    data object ShowActivityLogger : ProgressUiEvent
    data object DismissActivityLogger : ProgressUiEvent
    data class UpdateActivityType(val value: String) : ProgressUiEvent
    data class UpdateActivityDuration(val value: String) : ProgressUiEvent
    data class UpdateActivityExertion(val value: String) : ProgressUiEvent
    data class UpdateActivityCaloriesOverride(val value: String) : ProgressUiEvent
    data class UpdateActivityNotes(val value: String) : ProgressUiEvent
    data object SaveActivityLog : ProgressUiEvent
}

sealed interface ProgressEffect {
    data class Message(val value: String) : ProgressEffect
    data class RequestHealthPermissions(val permissions: Set<String>) : ProgressEffect
}
