package com.kazvoeten.omadketonics.data.repository.policy

import com.kazvoeten.omadketonics.domain.policy.IngredientCategoryInferencePolicy
import com.kazvoeten.omadketonics.model.IngredientCategory
import java.util.Locale
import javax.inject.Inject

class DefaultIngredientCategoryInferencePolicy @Inject constructor() : IngredientCategoryInferencePolicy {
    override fun inferCategory(name: String): IngredientCategory {
        val n = name.lowercase(Locale.US)
        return when {
            n.contains("chicken") || n.contains("beef") || n.contains("pork") || n.contains("fish") || n.contains("mackerel") || n.contains("tuna") -> IngredientCategory.Meat
            n.contains("yogurt") || n.contains("cheese") || n.contains("egg") || n.contains("tofu") -> IngredientCategory.Dairy
            n.contains("oil") || n.contains("sauce") || n.contains("mayo") || n.contains("powder") || n.contains("broth") || n.contains("butter") -> IngredientCategory.Pantry
            n.contains("frozen") || n.contains("blueberr") -> IngredientCategory.Freezer
            else -> IngredientCategory.Produce
        }
    }
}
