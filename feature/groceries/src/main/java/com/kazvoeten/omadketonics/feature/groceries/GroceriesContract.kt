package com.kazvoeten.omadketonics.feature.groceries

import com.kazvoeten.omadketonics.model.IngredientCategory

data class GroceryItemUi(
    val name: String,
    val checked: Boolean,
)

data class GrocerySectionUi(
    val category: IngredientCategory,
    val items: List<GroceryItemUi>,
)

data class GroceriesUiState(
    val isLoading: Boolean = true,
    val isViewingCurrentWeek: Boolean = true,
    val sections: List<GrocerySectionUi> = emptyList(),
)

sealed interface GroceriesUiEvent {
    data class ToggleItem(val itemName: String, val currentlyChecked: Boolean) : GroceriesUiEvent
}
