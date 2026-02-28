package com.kazvoeten.omadketonics.feature.recipes

import com.kazvoeten.omadketonics.model.Ingredient
import com.kazvoeten.omadketonics.model.OpenFoodFactsProduct
import com.kazvoeten.omadketonics.model.Recipe

data class RecipesUiState(
    val isLoading: Boolean = true,
    val isViewingCurrentWeek: Boolean = true,
    val recipes: List<Recipe> = emptyList(),
    val inPlanRecipeIds: Set<String> = emptySet(),
    val selectedRecipeId: String? = null,
    val editingRecipeId: String? = null,
)

data class RecipeEditorSearchState(
    val loading: Boolean = false,
    val error: String? = null,
    val results: List<OpenFoodFactsProduct> = emptyList(),
)

sealed interface RecipesUiEvent {
    data class OpenRecipe(val recipeId: String) : RecipesUiEvent
    data object CloseRecipe : RecipesUiEvent
    data class AddToCurrentWeek(val recipeId: String) : RecipesUiEvent
    data object StartCreate : RecipesUiEvent
    data class StartEdit(val recipeId: String) : RecipesUiEvent
    data object CloseEditor : RecipesUiEvent
    data class SearchIngredient(val query: String) : RecipesUiEvent
    data class SaveRecipe(
        val existingId: String?,
        val name: String,
        val ingredients: List<Ingredient>,
        val instructions: String,
    ) : RecipesUiEvent
}

sealed interface RecipesEffect {
    data class Message(val value: String) : RecipesEffect
}
