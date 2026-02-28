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
