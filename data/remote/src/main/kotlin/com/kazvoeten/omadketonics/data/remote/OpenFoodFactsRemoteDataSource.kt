package com.kazvoeten.omadketonics.data.remote

import com.kazvoeten.omadketonics.model.IngredientNutrition
import com.kazvoeten.omadketonics.model.OpenFoodFactsProduct
import com.kazvoeten.omadketonics.data.remote.api.OpenFoodFactsService
import javax.inject.Inject

class OpenFoodFactsRemoteDataSource @Inject constructor(
    private val service: OpenFoodFactsService,
) {
    suspend fun searchProducts(query: String, pageSize: Int = 12): List<OpenFoodFactsProduct> {
        val normalized = query.trim()
        if (normalized.isBlank()) return emptyList()

        val response = service.searchProducts(query = normalized, pageSize = pageSize)
        return response.products.orEmpty().mapNotNull { dto ->
            val code = dto.code?.trim().orEmpty()
            if (code.isBlank()) return@mapNotNull null

            val name = dto.productName?.trim().orEmpty().ifBlank { dto.productNameEn?.trim().orEmpty() }
            if (name.isBlank()) return@mapNotNull null

            val nutriments = dto.nutriments ?: return@mapNotNull null
            val calories = (nutriments.energyKcal100g ?: nutriments.energyKcal) ?: return@mapNotNull null
            val protein = nutriments.proteins100g ?: nutriments.proteins ?: 0.0
            val carbs = nutriments.carbohydrates100g ?: nutriments.carbohydrates ?: 0.0
            val fat = nutriments.fat100g ?: nutriments.fat ?: 0.0

            OpenFoodFactsProduct(
                code = code,
                name = name,
                brand = dto.brands?.trim()?.ifBlank { null },
                nutriScoreGrade = dto.nutriScoreGrade?.trim()?.uppercase()?.ifBlank { null },
                nutritionPer100g = IngredientNutrition(
                    calories = calories.toFloat(),
                    protein = protein.toFloat(),
                    carbs = carbs.toFloat(),
                    fat = fat.toFloat(),
                ),
            )
        }
    }
}
