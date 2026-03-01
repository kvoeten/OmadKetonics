package com.kazvoeten.omadketonics.model

import java.time.LocalDate

enum class IngredientCategory {
    Produce,
    Meat,
    Dairy,
    Pantry,
    Freezer,
}

data class Ingredient(
    val name: String,
    val category: IngredientCategory,
    val amountGrams: Float = 100f,
    val apiProductCode: String? = null,
    val apiProductName: String? = null,
    val apiBrand: String? = null,
    val apiNutriScore: String? = null,
    val nutrientsPer100g: IngredientNutrition? = null,
)

data class IngredientNutrition(
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
)

data class OpenFoodFactsProduct(
    val code: String,
    val name: String,
    val brand: String?,
    val nutriScoreGrade: String?,
    val nutritionPer100g: IngredientNutrition,
)

data class Recipe(
    val id: String,
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val icon: String = "\uD83C\uDF7D\uFE0F",
    val imageUri: String? = null,
)

data class MealHistoryEntry(
    val date: LocalDate,
    val mealId: String,
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
)

data class MacroAverages(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
)

data class DayTrend(
    val date: LocalDate,
    val dayLabel: String,
    val weight: Float?,
    val calories: Int,
)

enum class DailyMood(
    val label: String,
) {
    Terrible("Terrible"),
    Low("Low"),
    Okay("Okay"),
    Good("Good"),
    Great("Great"),
}

data class WeekSnapshot(
    val startDate: LocalDate,
    val mealIds: List<String>,
    val eatenMealIds: List<String>,
)

enum class OmadTab(val label: String) {
    Plan("Plan"),
    Groceries("List"),
    Recipes("Recipes"),
    Rankings("Ranks"),
    Progress("Progress"),
}

enum class ChartType {
    Weight,
    Calories,
}

enum class HealthAvailability {
    Unavailable,
    ProviderUpdateRequired,
    Available,
}

data class HealthConnectionState(
    val availability: HealthAvailability = HealthAvailability.Unavailable,
    val hasPermissions: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncedAtEpochMillis: Long? = null,
    val pendingOutboxCount: Int = 0,
    val lastError: String? = null,
)

data class SleepSummary(
    val totalSleepMinutes: Int = 0,
    val deepSleepMinutes: Int = 0,
    val remSleepMinutes: Int = 0,
    val lightSleepMinutes: Int = 0,
    val sessionCount: Int = 0,
)

data class ActivitySummary(
    val exerciseMinutes: Int = 0,
    val activeCalories: Int = 0,
    val sessionCount: Int = 0,
    val highIntensitySessions: Int = 0,
    val moderateIntensitySessions: Int = 0,
    val lowIntensitySessions: Int = 0,
)

data class DailyHealthSummary(
    val date: LocalDate,
    val sleep: SleepSummary = SleepSummary(),
    val activity: ActivitySummary = ActivitySummary(),
    val source: String = "health_connect",
)

enum class ManualActivitySource {
    AppManual,
    External,
}

data class ManualActivityInput(
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val activityType: String,
    val exertion: Int,
    val caloriesOverride: Int? = null,
    val notes: String? = null,
)

data class ManualActivityLog(
    val id: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val activityType: String,
    val exertion: Int,
    val calories: Int,
    val source: ManualActivitySource,
    val outboxStatus: HealthOutboxStatus,
    val healthClientRecordId: String?,
    val createdAtEpochMillis: Long,
    val syncedAtEpochMillis: Long? = null,
)

enum class HealthOutboxType {
    ActivityUpsert,
    NutritionUpsert,
    NutritionDelete,
}

enum class HealthOutboxStatus {
    Pending,
    Processing,
    Synced,
    Failed,
}

enum class InsightRange {
    Day,
    Week,
    Month,
}

enum class ProgressDeepLinkMetric {
    ActivityRings,
    ExerciseLoad,
    WeightTrend,
    CalorieConsistency,
    SleepQuality,
}

data class ProgressInsights(
    val recoveryScore: Int,
    val sleepGoalMetDays: Int,
    val averageSleepMinutes: Int,
    val weeklyExerciseMinutes: Int,
    val weeklyActiveCalories: Int,
    val activityIntensityDeltaPct: Int,
    val moodCorrelationPct: Int,
    val calorieAverage: Int,
    val caloriePeak: Int,
    val narrativeSleep: String,
    val narrativeActivity: String,
    val narrativeCalories: String,
)
