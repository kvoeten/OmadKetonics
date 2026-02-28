package com.kazvoeten.omadketonics.data.repository.impl

import androidx.room.withTransaction
import com.kazvoeten.omadketonics.data.local.OmadDatabase
import com.kazvoeten.omadketonics.data.local.dao.RecipeDao
import com.kazvoeten.omadketonics.data.local.mapper.toDomain
import com.kazvoeten.omadketonics.data.local.mapper.toEntity
import com.kazvoeten.omadketonics.data.local.mapper.toIngredientEntities
import com.kazvoeten.omadketonics.data.local.mapper.toInstructionEntities
import com.kazvoeten.omadketonics.data.repository.seed.DefaultIngredientSeeds
import com.kazvoeten.omadketonics.data.repository.seed.RecipeCatalog
import com.kazvoeten.omadketonics.domain.repository.RecipeRepository
import com.kazvoeten.omadketonics.model.Ingredient
import com.kazvoeten.omadketonics.model.IngredientCategory
import com.kazvoeten.omadketonics.model.IngredientNutrition
import com.kazvoeten.omadketonics.model.MacroAverages
import com.kazvoeten.omadketonics.model.Recipe
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val database: OmadDatabase,
    private val recipeDao: RecipeDao,
) : RecipeRepository {
    override fun observeRecipes(): Flow<List<Recipe>> {
        return recipeDao.observeRecipesWithDetails().map { items ->
            items.map { it.toDomain() }
        }
    }

    override suspend fun getRecipes(): List<Recipe> {
        return recipeDao.getRecipesWithDetails().map { it.toDomain() }
    }

    override suspend fun getRecipe(recipeId: String): Recipe? {
        return recipeDao.getRecipeWithDetails(recipeId)?.toDomain()
    }

    override suspend fun saveRecipe(recipe: Recipe) {
        database.withTransaction {
            recipeDao.upsertRecipe(recipe.toEntity())
            recipeDao.deleteIngredientsForRecipe(recipe.id)
            recipeDao.deleteInstructionsForRecipe(recipe.id)
            recipeDao.upsertIngredients(recipe.toIngredientEntities())
            recipeDao.upsertInstructions(recipe.toInstructionEntities())
        }
    }

    override suspend fun ensureSeeded() {
        if (recipeDao.countRecipes() > 0) return

        val seedRecipes = seedRecipesFromCatalog()
        database.withTransaction {
            seedRecipes.forEach { recipe ->
                recipeDao.upsertRecipe(recipe.toEntity())
                recipeDao.upsertIngredients(recipe.toIngredientEntities())
                recipeDao.upsertInstructions(recipe.toInstructionEntities())
            }
        }
    }

    private fun seedRecipesFromCatalog(): List<Recipe> {
        return RecipeCatalog.recipes.map { recipe ->
            val normalizedIngredients = recipe.ingredients.map(::convertLegacyIngredient).map(DefaultIngredientSeeds::apply)
            val macro = if (normalizedIngredients.all { it.nutrientsPer100g != null }) {
                calculateRecipeMacros(normalizedIngredients)
            } else {
                MacroAverages(recipe.calories, recipe.protein, recipe.carbs, recipe.fat)
            }

            recipe.copy(
                calories = macro.calories,
                protein = macro.protein,
                carbs = macro.carbs,
                fat = macro.fat,
                ingredients = normalizedIngredients,
            )
        }
    }

    private fun convertLegacyIngredient(ingredient: Ingredient): Ingredient {
        val rawName = ingredient.name.trim()
        val cleanedName = rawName.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
        val grams = parseLegacyAmountGrams(rawName, ingredient.category)
        return ingredient.copy(
            name = cleanedName.ifBlank { rawName },
            amountGrams = grams,
            apiProductCode = null,
            apiProductName = null,
            apiBrand = null,
            apiNutriScore = null,
            nutrientsPer100g = null,
        )
    }

    private fun parseLegacyAmountGrams(name: String, category: IngredientCategory): Float {
        val gramsMatch = Regex("\\((\\d+(?:\\.\\d+)?)\\s*g\\)", RegexOption.IGNORE_CASE).find(name)
        if (gramsMatch != null) {
            return gramsMatch.groupValues[1].toFloatOrNull()?.coerceAtLeast(1f) ?: 100f
        }
        val piecesMatch = Regex("\\((\\d+)\\s*pcs\\)", RegexOption.IGNORE_CASE).find(name)
        if (piecesMatch != null) {
            val count = piecesMatch.groupValues[1].toFloatOrNull() ?: 1f
            return (count * 60f).coerceAtLeast(60f)
        }

        val lower = name.lowercase(Locale.US)
        return when {
            lower.contains("egg") -> 150f
            lower.contains("olive oil") -> 30f
            lower.contains("sesame oil") -> 20f
            lower.contains("soy sauce") -> 20f
            lower.contains("mayo") -> 30f
            lower.contains("cheese") -> 80f
            lower.contains("yogurt") -> 170f
            lower.contains("blueberr") -> 100f
            lower.contains("broth") -> 400f
            lower.contains("powder") -> 8f
            lower.contains("butter") -> 32f
            lower.contains("tofu") -> 180f
            category == IngredientCategory.Meat -> 220f
            category == IngredientCategory.Produce -> 100f
            category == IngredientCategory.Dairy -> 120f
            category == IngredientCategory.Pantry -> 25f
            category == IngredientCategory.Freezer -> 100f
            else -> 100f
        }
    }

    private fun calculateRecipeMacros(ingredients: List<Ingredient>): MacroAverages {
        var calories = 0f
        var protein = 0f
        var carbs = 0f
        var fat = 0f

        ingredients.forEach { ingredient ->
            val nutrition: IngredientNutrition = ingredient.nutrientsPer100g ?: return@forEach
            val factor = ingredient.amountGrams.coerceAtLeast(0f) / 100f
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
}
