package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.domain.model.SaveRecipeRequest
import com.kazvoeten.omadketonics.domain.repository.RecipeRepository
import com.kazvoeten.omadketonics.model.MacroAverages
import com.kazvoeten.omadketonics.model.Recipe
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

class SaveRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
) {
    suspend operator fun invoke(request: SaveRecipeRequest): String? {
        val trimmedName = request.name.trim()
        if (trimmedName.isBlank()) return "Meal name is required."
        if (request.ingredients.isEmpty()) return "Add at least one ingredient."

        val cleanedIngredients = request.ingredients
            .map { it.copy(name = it.name.trim()) }
            .filter { it.name.isNotBlank() }
        if (cleanedIngredients.isEmpty()) return "Add at least one valid ingredient."
        if (cleanedIngredients.any { it.nutrientsPer100g == null }) {
            return "Every ingredient must come from OpenFoodFacts before saving."
        }

        val instructions = request.instructionsInput.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (instructions.isEmpty()) return "Add at least one prep step."

        val macro = calculateRecipeMacros(cleanedIngredients)
        if (macro.calories <= 0) return "Calculated calories are zero. Adjust ingredient amounts."

        val recipeId = request.existingId ?: generateRecipeId(trimmedName, recipeRepository.getRecipes())
        val recipe = Recipe(
            id = recipeId,
            name = trimmedName,
            calories = macro.calories,
            protein = macro.protein,
            carbs = macro.carbs,
            fat = macro.fat,
            ingredients = cleanedIngredients,
            instructions = instructions,
        )

        recipeRepository.saveRecipe(recipe)
        return null
    }

    private fun calculateRecipeMacros(ingredients: List<com.kazvoeten.omadketonics.model.Ingredient>): MacroAverages {
        var calories = 0f
        var protein = 0f
        var carbs = 0f
        var fat = 0f
        ingredients.forEach { ingredient ->
            val nutrition = ingredient.nutrientsPer100g ?: return@forEach
            val grams = ingredient.amountGrams.coerceAtLeast(0f)
            val factor = grams / 100f
            calories += nutrition.calories * factor
            protein += nutrition.protein * factor
            carbs += nutrition.carbs * factor
            fat += nutrition.fat * factor
        }

        return MacroAverages(
            calories = calories.roundToInt(),
            protein = protein.roundToInt(),
            carbs = carbs.roundToInt(),
            fat = fat.roundToInt(),
        )
    }

    private fun generateRecipeId(name: String, recipes: List<Recipe>): String {
        val slug = name.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "meal" }
        val base = "r-$slug"
        if (recipes.none { it.id == base }) return base
        var suffix = 2
        while (recipes.any { it.id == "$base-$suffix" }) {
            suffix += 1
        }
        return "$base-$suffix"
    }
}
