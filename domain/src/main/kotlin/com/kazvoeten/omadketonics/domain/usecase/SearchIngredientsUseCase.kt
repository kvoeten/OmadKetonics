package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.core.common.AppResult
import com.kazvoeten.omadketonics.domain.repository.IngredientSearchRepository
import com.kazvoeten.omadketonics.model.OpenFoodFactsProduct
import javax.inject.Inject

class SearchIngredientsUseCase @Inject constructor(
    private val ingredientSearchRepository: IngredientSearchRepository,
) {
    suspend operator fun invoke(query: String): AppResult<List<OpenFoodFactsProduct>> {
        if (query.isBlank()) return AppResult.Success(emptyList())
        return ingredientSearchRepository.searchProducts(query)
    }
}
