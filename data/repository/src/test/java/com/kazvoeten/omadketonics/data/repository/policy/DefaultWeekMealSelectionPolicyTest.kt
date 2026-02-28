package com.kazvoeten.omadketonics.data.repository.policy

import com.kazvoeten.omadketonics.model.Recipe
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultWeekMealSelectionPolicyTest {
    private val policy = DefaultWeekMealSelectionPolicy()

    @Test
    fun `top rated recipes are always selected`() {
        val recipes = (1..10).map { i ->
            Recipe(
                id = "r$i",
                name = "Recipe $i",
                calories = 100,
                protein = 10,
                carbs = 10,
                fat = 10,
                ingredients = emptyList(),
                instructions = emptyList(),
            )
        }
        val ratings = mapOf("r1" to 5, "r2" to 4, "r3" to 3)

        val selected = policy.selectMealIds(recipes, ratings, weekSize = 7)

        assertTrue(selected.contains("r1"))
        assertTrue(selected.contains("r2"))
        assertTrue(selected.contains("r3"))
        assertTrue(selected.size <= 7)
    }
}
