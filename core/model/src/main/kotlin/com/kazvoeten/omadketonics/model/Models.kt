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
