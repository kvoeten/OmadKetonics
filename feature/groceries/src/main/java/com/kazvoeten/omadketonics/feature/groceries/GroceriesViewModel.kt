package com.kazvoeten.omadketonics.feature.groceries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazvoeten.omadketonics.domain.model.DisplayedWeek
import com.kazvoeten.omadketonics.domain.repository.GroceryRepository
import com.kazvoeten.omadketonics.domain.repository.RecipeRepository
import com.kazvoeten.omadketonics.domain.usecase.GetDisplayedWeekUseCase
import com.kazvoeten.omadketonics.domain.usecase.ToggleGroceryItemUseCase
import com.kazvoeten.omadketonics.model.IngredientCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class GroceriesViewModel @Inject constructor(
    private val getDisplayedWeekUseCase: GetDisplayedWeekUseCase,
    private val recipeRepository: RecipeRepository,
    private val groceryRepository: GroceryRepository,
    private val toggleGroceryItemUseCase: ToggleGroceryItemUseCase,
) : ViewModel() {
    private var latestDisplayedWeek: DisplayedWeek? = null

    val state = getDisplayedWeekUseCase().filterNotNull().flatMapLatest { displayedWeek ->
        latestDisplayedWeek = displayedWeek
        combine(
            recipeRepository.observeRecipes(),
            groceryRepository.observeChecklist(displayedWeek.snapshot.startDate),
        ) { recipes, checklist ->
            val recipeById = recipes.associateBy { it.id }
            val displayedRecipes = displayedWeek.snapshot.mealIds.mapNotNull { recipeById[it] }

            val order = listOf(
                IngredientCategory.Produce,
                IngredientCategory.Meat,
                IngredientCategory.Dairy,
                IngredientCategory.Pantry,
                IngredientCategory.Freezer,
            )
            val added = linkedSetOf<String>()
            val sections = order.mapNotNull { category ->
                val items = displayedRecipes.flatMap { it.ingredients }
                    .filter { it.category == category }
                    .mapNotNull { ingredient ->
                        if (added.add(ingredient.name)) ingredient.name else null
                    }
                    .map { name ->
                        GroceryItemUi(
                            name = name,
                            checked = if (displayedWeek.isViewingCurrentWeek) checklist[name] ?: false else false,
                        )
                    }
                if (items.isEmpty()) null else GrocerySectionUi(category = category, items = items)
            }

            GroceriesUiState(
                isLoading = false,
                isViewingCurrentWeek = displayedWeek.isViewingCurrentWeek,
                sections = sections,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroceriesUiState(),
    )

    fun onEvent(event: GroceriesUiEvent) {
        when (event) {
            is GroceriesUiEvent.ToggleItem -> {
                viewModelScope.launch {
                    val uiState = state.value
                    if (!uiState.isViewingCurrentWeek) return@launch
                    val displayedWeek = latestDisplayedWeek ?: return@launch
                    toggleGroceryItemUseCase(
                        weekStartDate = displayedWeek.snapshot.startDate,
                        itemName = event.itemName,
                        isChecked = event.currentlyChecked,
                    )
                }
            }
        }
    }
}
