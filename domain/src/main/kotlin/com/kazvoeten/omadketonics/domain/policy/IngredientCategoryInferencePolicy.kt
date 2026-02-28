package com.kazvoeten.omadketonics.domain.policy

import com.kazvoeten.omadketonics.model.IngredientCategory

interface IngredientCategoryInferencePolicy {
    fun inferCategory(name: String): IngredientCategory
}
