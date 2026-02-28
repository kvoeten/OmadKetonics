package com.kazvoeten.omadketonics.domain.repository

import com.kazvoeten.omadketonics.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun observeRecipes(): Flow<List<Recipe>>
    suspend fun getRecipes(): List<Recipe>
    suspend fun getRecipe(recipeId: String): Recipe?
    suspend fun saveRecipe(recipe: Recipe)
    suspend fun ensureSeeded()
}
