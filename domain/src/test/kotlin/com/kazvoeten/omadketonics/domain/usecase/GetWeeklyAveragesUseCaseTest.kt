package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.model.Recipe
import org.junit.Assert.assertEquals
import org.junit.Test

class GetWeeklyAveragesUseCaseTest {
    private val useCase = GetWeeklyAveragesUseCase()

    @Test
    fun `returns zero averages for empty list`() {
        val result = useCase(emptyList())
        assertEquals(0, result.calories)
        assertEquals(0, result.protein)
        assertEquals(0, result.carbs)
        assertEquals(0, result.fat)
    }

    @Test
    fun `returns average macros`() {
        val recipes = listOf(
            Recipe("a", "A", 100, 10, 5, 2, emptyList(), emptyList()),
            Recipe("b", "B", 300, 30, 15, 6, emptyList(), emptyList()),
        )

        val result = useCase(recipes)
        assertEquals(200, result.calories)
        assertEquals(20, result.protein)
        assertEquals(10, result.carbs)
        assertEquals(4, result.fat)
    }
}
