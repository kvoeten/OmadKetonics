package com.kazvoeten.omadketonics.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    @ColumnInfo(name = "recipe_icon") val recipeIcon: String,
    @ColumnInfo(name = "recipe_image_uri") val recipeImageUri: String?,
)

@Entity(
    tableName = "recipe_ingredients",
    primaryKeys = ["recipe_id", "position"],
    indices = [Index("recipe_id")],
)
data class RecipeIngredientEntity(
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    val position: Int,
    val name: String,
    val category: String,
    @ColumnInfo(name = "amount_grams") val amountGrams: Float,
    @ColumnInfo(name = "api_product_code") val apiProductCode: String?,
    @ColumnInfo(name = "api_product_name") val apiProductName: String?,
    @ColumnInfo(name = "api_brand") val apiBrand: String?,
    @ColumnInfo(name = "api_nutri_score") val apiNutriScore: String?,
    @ColumnInfo(name = "nutrition_calories") val nutritionCalories: Float?,
    @ColumnInfo(name = "nutrition_protein") val nutritionProtein: Float?,
    @ColumnInfo(name = "nutrition_carbs") val nutritionCarbs: Float?,
    @ColumnInfo(name = "nutrition_fat") val nutritionFat: Float?,
)

@Entity(
    tableName = "recipe_instructions",
    primaryKeys = ["recipe_id", "step_index"],
    indices = [Index("recipe_id")],
)
data class RecipeInstructionEntity(
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "step_index") val stepIndex: Int,
    val text: String,
)

@Entity(
    tableName = "week_plans",
    indices = [Index("start_date")],
)
data class WeekPlanEntity(
    @PrimaryKey
    @ColumnInfo(name = "start_date")
    val startDate: String,
    @ColumnInfo(name = "is_current")
    val isCurrent: Boolean,
)

@Entity(
    tableName = "week_plan_meals",
    primaryKeys = ["week_start_date", "recipe_id"],
    indices = [Index("week_start_date"), Index("week_start_date", "position")],
)
data class WeekPlanMealEntity(
    @ColumnInfo(name = "week_start_date")
    val weekStartDate: String,
    @ColumnInfo(name = "recipe_id")
    val recipeId: String,
    val position: Int,
    @ColumnInfo(name = "is_eaten")
    val isEaten: Boolean,
)

@Entity(
    tableName = "meal_history",
    indices = [Index("date")],
)
data class MealHistoryEntity(
    @PrimaryKey
    val date: String,
    @ColumnInfo(name = "meal_id")
    val mealId: String,
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
)

@Entity(
    tableName = "weight_logs",
    indices = [Index("date")],
)
data class WeightLogEntity(
    @PrimaryKey
    val date: String,
    val weight: Float,
)

@Entity(
    tableName = "mood_logs",
    indices = [Index("date")],
)
data class MoodLogEntity(
    @PrimaryKey
    val date: String,
    val mood: String,
)

@Entity(
    tableName = "ratings",
    indices = [Index("recipe_id")],
)
data class RatingEntity(
    @PrimaryKey
    @ColumnInfo(name = "recipe_id")
    val recipeId: String,
    val rating: Int,
)

@Entity(
    tableName = "grocery_checks",
    primaryKeys = ["week_start", "item_name"],
    indices = [Index("week_start", "item_name")],
)
data class GroceryCheckEntity(
    @ColumnInfo(name = "week_start")
    val weekStart: String,
    @ColumnInfo(name = "item_name")
    val itemName: String,
    @ColumnInfo(name = "checked")
    val checked: Boolean,
)

@Entity(
    tableName = "ingredient_search_cache",
    primaryKeys = ["query", "code"],
    indices = [Index("query")],
)
data class IngredientSearchCacheEntity(
    val query: String,
    val code: String,
    val name: String,
    val brand: String?,
    @ColumnInfo(name = "nutri_score")
    val nutriScore: String?,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    @ColumnInfo(name = "created_at_epoch")
    val createdAtEpoch: Long,
)

@Entity(
    tableName = "health_daily_summary",
    indices = [Index("date")],
)
data class HealthDailySummaryEntity(
    @PrimaryKey
    val date: String,
    @ColumnInfo(name = "sleep_total_minutes")
    val sleepTotalMinutes: Int,
    @ColumnInfo(name = "sleep_deep_minutes")
    val sleepDeepMinutes: Int,
    @ColumnInfo(name = "sleep_rem_minutes")
    val sleepRemMinutes: Int,
    @ColumnInfo(name = "sleep_light_minutes")
    val sleepLightMinutes: Int,
    @ColumnInfo(name = "sleep_session_count")
    val sleepSessionCount: Int,
    @ColumnInfo(name = "exercise_minutes")
    val exerciseMinutes: Int,
    @ColumnInfo(name = "active_calories")
    val activeCalories: Int,
    @ColumnInfo(name = "activity_session_count")
    val activitySessionCount: Int,
    @ColumnInfo(name = "high_intensity_sessions")
    val highIntensitySessions: Int,
    @ColumnInfo(name = "moderate_intensity_sessions")
    val moderateIntensitySessions: Int,
    @ColumnInfo(name = "low_intensity_sessions")
    val lowIntensitySessions: Int,
    val source: String,
    @ColumnInfo(name = "updated_at_epoch")
    val updatedAtEpoch: Long,
)

@Entity(
    tableName = "manual_activity_logs",
    indices = [Index("start_time_epoch"), Index("outbox_status"), Index("health_client_record_id")],
)
data class ManualActivityLogEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "start_time_epoch")
    val startTimeEpochMillis: Long,
    @ColumnInfo(name = "end_time_epoch")
    val endTimeEpochMillis: Long,
    @ColumnInfo(name = "activity_type")
    val activityType: String,
    val exertion: Int,
    val calories: Int,
    val source: String,
    @ColumnInfo(name = "outbox_status")
    val outboxStatus: String,
    @ColumnInfo(name = "health_client_record_id")
    val healthClientRecordId: String?,
    @ColumnInfo(name = "notes")
    val notes: String?,
    @ColumnInfo(name = "created_at_epoch")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "synced_at_epoch")
    val syncedAtEpochMillis: Long?,
)

@Entity(
    tableName = "health_outbox",
    indices = [Index("status"), Index("item_type"), Index("created_at_epoch")],
)
data class HealthOutboxEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "item_type")
    val itemType: String,
    @ColumnInfo(name = "payload_json")
    val payloadJson: String,
    val status: String,
    val attempts: Int,
    @ColumnInfo(name = "last_error")
    val lastError: String?,
    @ColumnInfo(name = "created_at_epoch")
    val createdAtEpoch: Long,
    @ColumnInfo(name = "updated_at_epoch")
    val updatedAtEpoch: Long,
)

@Entity(tableName = "health_sync_state")
data class HealthSyncStateEntity(
    @PrimaryKey
    val key: String,
    @ColumnInfo(name = "value_text")
    val valueText: String? = null,
    @ColumnInfo(name = "value_long")
    val valueLong: Long? = null,
)
