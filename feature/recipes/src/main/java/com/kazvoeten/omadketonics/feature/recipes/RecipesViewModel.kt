package com.kazvoeten.omadketonics.feature.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazvoeten.omadketonics.core.common.AppResult
import com.kazvoeten.omadketonics.domain.model.DisplayedWeek
import com.kazvoeten.omadketonics.domain.model.SaveRecipeRequest
import com.kazvoeten.omadketonics.domain.policy.IngredientCategoryInferencePolicy
import com.kazvoeten.omadketonics.domain.repository.RecipeRepository
import com.kazvoeten.omadketonics.domain.repository.WeekPlanRepository
import com.kazvoeten.omadketonics.domain.usecase.GetDisplayedWeekUseCase
import com.kazvoeten.omadketonics.domain.usecase.SaveRecipeUseCase
import com.kazvoeten.omadketonics.domain.usecase.SearchIngredientsUseCase
import com.kazvoeten.omadketonics.model.Ingredient
import com.kazvoeten.omadketonics.model.IngredientCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val NEW_RECIPE_SENTINEL = "__new_recipe__"

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val weekPlanRepository: WeekPlanRepository,
    private val getDisplayedWeekUseCase: GetDisplayedWeekUseCase,
    private val searchIngredientsUseCase: SearchIngredientsUseCase,
    private val saveRecipeUseCase: SaveRecipeUseCase,
    private val ingredientCategoryInferencePolicy: IngredientCategoryInferencePolicy,
) : ViewModel() {
    private val selectedRecipeId = MutableStateFlow<String?>(null)
    private val editingRecipeId = MutableStateFlow<String?>(null)
    private var latestDisplayedWeek: DisplayedWeek? = null
    private var searchJob: Job? = null

    private val _searchState = MutableStateFlow(RecipeEditorSearchState())
    val searchState = _searchState

    private val effectEmitter = MutableSharedFlow<RecipesEffect>(extraBufferCapacity = 8)
    val effects = effectEmitter

    val state = combine(
        getDisplayedWeekUseCase().filterNotNull(),
        recipeRepository.observeRecipes(),
        selectedRecipeId,
        editingRecipeId,
    ) { displayedWeek, recipes, selectedId, editingId ->
        latestDisplayedWeek = displayedWeek
        RecipesUiState(
            isLoading = false,
            isViewingCurrentWeek = displayedWeek.isViewingCurrentWeek,
            recipes = recipes,
            inPlanRecipeIds = displayedWeek.snapshot.mealIds.toSet(),
            selectedRecipeId = selectedId,
            editingRecipeId = editingId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipesUiState(),
    )

    fun editingRecipe(): RecipeEditorSeed? {
        val currentState = state.value
        val id = currentState.editingRecipeId ?: return null
        if (id == NEW_RECIPE_SENTINEL) {
            return RecipeEditorSeed(
                existingId = null,
                name = "",
                ingredients = emptyList(),
                instructions = "",
            )
        }
        val recipe = currentState.recipes.firstOrNull { it.id == id } ?: return null
        return RecipeEditorSeed(
            existingId = recipe.id,
            name = recipe.name,
            ingredients = recipe.ingredients,
            instructions = recipe.instructions.joinToString("\n"),
        )
    }

    fun selectedRecipe() = state.value.recipes.firstOrNull { it.id == state.value.selectedRecipeId }

    fun onEvent(event: RecipesUiEvent) {
        when (event) {
            is RecipesUiEvent.OpenRecipe -> selectedRecipeId.value = event.recipeId
            is RecipesUiEvent.CloseRecipe -> selectedRecipeId.value = null
            is RecipesUiEvent.StartCreate -> {
                editingRecipeId.value = NEW_RECIPE_SENTINEL
                _searchState.value = RecipeEditorSearchState()
            }

            is RecipesUiEvent.StartEdit -> {
                editingRecipeId.value = event.recipeId
                _searchState.value = RecipeEditorSearchState()
            }

            is RecipesUiEvent.CloseEditor -> editingRecipeId.value = null
            is RecipesUiEvent.AddToCurrentWeek -> {
                viewModelScope.launch {
                    val displayedWeek = latestDisplayedWeek ?: return@launch
                    if (!displayedWeek.isViewingCurrentWeek) {
                        effectEmitter.emit(RecipesEffect.Message("Historical week is read-only"))
                        return@launch
                    }
                    weekPlanRepository.addMealToCurrentWeek(event.recipeId)
                    effectEmitter.emit(RecipesEffect.Message("Added to current week"))
                }
            }

            is RecipesUiEvent.SearchIngredient -> searchIngredients(event.query)
            is RecipesUiEvent.SaveRecipe -> {
                viewModelScope.launch {
                    val error = saveRecipeUseCase(
                        SaveRecipeRequest(
                            existingId = event.existingId,
                            name = event.name,
                            ingredients = event.ingredients,
                            instructionsInput = event.instructions,
                        ),
                    )
                    if (error != null) {
                        effectEmitter.emit(RecipesEffect.Message(error))
                    } else {
                        editingRecipeId.value = null
                        effectEmitter.emit(RecipesEffect.Message("Recipe saved"))
                    }
                }
            }
        }
    }

    fun inferCategory(name: String): IngredientCategory = ingredientCategoryInferencePolicy.inferCategory(name)

    fun cycleCategory(current: IngredientCategory): IngredientCategory {
        val entries = IngredientCategory.entries
        val idx = entries.indexOf(current)
        if (idx < 0) return IngredientCategory.Produce
        return entries[(idx + 1) % entries.size]
    }

    private fun searchIngredients(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            _searchState.value = RecipeEditorSearchState()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchState.value = RecipeEditorSearchState(loading = true)
            when (val result = searchIngredientsUseCase(trimmed)) {
                is AppResult.Success -> {
                    _searchState.value = RecipeEditorSearchState(
                        loading = false,
                        results = result.value,
                    )
                }

                is AppResult.Error -> {
                    _searchState.value = RecipeEditorSearchState(
                        loading = false,
                        error = result.message,
                    )
                }
            }
        }
    }
}

data class RecipeEditorSeed(
    val existingId: String?,
    val name: String,
    val ingredients: List<Ingredient>,
    val instructions: String,
)
