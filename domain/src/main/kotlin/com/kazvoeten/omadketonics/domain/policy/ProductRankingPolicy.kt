package com.kazvoeten.omadketonics.domain.policy

import com.kazvoeten.omadketonics.model.OpenFoodFactsProduct

interface ProductRankingPolicy {
    fun rank(query: String, products: List<OpenFoodFactsProduct>): List<OpenFoodFactsProduct>
}
