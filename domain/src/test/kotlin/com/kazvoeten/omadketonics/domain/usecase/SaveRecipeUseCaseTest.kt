package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.domain.model.SaveRecipeRequest
import com.kazvoeten.omadketonics.domain.repository.RecipeRepository
import com.kazvoeten.omadketonics.model.Ingredient
import com.kazvoeten.omadketonics.model.IngredientCategory
import com.kazvoeten.omadketonics.model.IngredientNutrition
import com.kazvoeten.omadketonics.model.Recipe
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SaveRecipeUseCaseTest {
    @Test
    fun `rejects empty name`() = runBlocking {
        val repo = FakeRecipeRepository()
        val useCase = SaveRecipeUseCase(repo)
        val error = useCase(
            SaveRecipeRequest(
                existingId = null,
                name = "  ",
                icon = "🍗",
                ingredients = listOf(sampleIngredient()),
                instructionsInput = "step",
            ),
        )
        assertEquals("Meal name is required.", error)
    }

    @Test
    fun `saves valid recipe`() = runBlocking {
        val repo = FakeRecipeRepository()
        val useCase = SaveRecipeUseCase(repo)
        val error = useCase(
            SaveRecipeRequest(
                existingId = null,
                name = "Chicken Bowl",
                icon = "🍲",
                ingredients = listOf(sampleIngredient()),
                instructionsInput = "Cook\nEat",
            ),
        )
        assertNull(error)
        assertNotNull(repo.saved)
        assertEquals("Chicken Bowl", repo.saved?.name)
    }

    private fun sampleIngredient(): Ingredient {
        return Ingredient(
            name = "Chicken Breast",
            category = IngredientCategory.Meat,
            amountGrams = 100f,
            nutrientsPer100g = IngredientNutrition(
                calories = 120f,
                protein = 22f,
                carbs = 0f,
                fat = 2.6f,
            ),
        )
    }

    private class FakeRecipeRepository : RecipeRepository {
        var saved: Recipe? = null
        override fun observeRecipes() = kotlinx.coroutines.flow.flowOf(emptyList<Recipe>())
        override suspend fun getRecipes(): List<Recipe> = emptyList()
        override suspend fun getRecipe(recipeId: String): Recipe? = null
        override suspend fun saveRecipe(recipe: Recipe) {
            saved = recipe
        }
        override suspend fun ensureSeeded() = Unit
    }
}
