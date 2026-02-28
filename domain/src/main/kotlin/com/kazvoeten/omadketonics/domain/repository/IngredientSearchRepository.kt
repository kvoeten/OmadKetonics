package com.kazvoeten.omadketonics.domain.repository

import com.kazvoeten.omadketonics.core.common.AppResult
import com.kazvoeten.omadketonics.model.OpenFoodFactsProduct

interface IngredientSearchRepository {
    suspend fun searchProducts(query: String): AppResult<List<OpenFoodFactsProduct>>
}
