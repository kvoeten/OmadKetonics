package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.domain.repository.HealthRepository
import com.kazvoeten.omadketonics.model.DailyHealthSummary
import com.kazvoeten.omadketonics.model.HealthConnectionState
import com.kazvoeten.omadketonics.model.ManualActivityInput
import com.kazvoeten.omadketonics.model.ManualActivityLog
import com.kazvoeten.omadketonics.model.MealHistoryEntry
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class QueueMealNutritionWriteUseCaseTest {
    @Test
    fun `upsert queues nutrition payload`() = runBlocking {
        val fake = FakeHealthRepository()
        val useCase = QueueMealNutritionWriteUseCase(fake)

        useCase.upsert(
            MealHistoryEntry(
                date = LocalDate.of(2026, 3, 1),
                mealId = "a",
                name = "OMAD",
                calories = 1800,
                protein = 130,
                carbs = 60,
                fat = 90,
            ),
        )

        assertEquals(1, fake.nutritionUpserts.size)
        assertEquals(1800, fake.nutritionUpserts.first().calories)
    }

    @Test
    fun `delete queues nutrition deletion by date`() = runBlocking {
        val fake = FakeHealthRepository()
        val useCase = QueueMealNutritionWriteUseCase(fake)

        val day = LocalDate.of(2026, 2, 28)
        useCase.delete(day)

        assertEquals(listOf(day), fake.nutritionDeletes)
    }

    private data class NutritionUpsert(
        val date: LocalDate,
        val mealName: String,
        val calories: Int,
        val protein: Int,
        val carbs: Int,
        val fat: Int,
    )

    private class FakeHealthRepository : HealthRepository {
        val nutritionUpserts = mutableListOf<NutritionUpsert>()
        val nutritionDeletes = mutableListOf<LocalDate>()

        override val connectionState: Flow<HealthConnectionState> = flowOf(HealthConnectionState())

        override fun requiredPermissions(): Set<String> = emptySet()

        override suspend fun updateGrantedPermissions(grantedPermissions: Set<String>) = Unit

        override fun observeDailySummaries(
            startDate: LocalDate,
            endDate: LocalDate,
        ): Flow<List<DailyHealthSummary>> = flowOf(emptyList())

        override fun observeManualActivityLogs(
            startDate: LocalDate,
            endDate: LocalDate,
        ): Flow<List<ManualActivityLog>> = flowOf(emptyList())

        override suspend fun queueManualActivity(
            input: ManualActivityInput,
            estimatedCalories: Int,
            clientRecordId: String,
            source: String,
        ) = Unit

        override suspend fun queueNutritionUpsert(
            date: LocalDate,
            mealName: String,
            calories: Int,
            protein: Int,
            carbs: Int,
            fat: Int,
        ) {
            nutritionUpserts += NutritionUpsert(date, mealName, calories, protein, carbs, fat)
        }

        override suspend fun queueNutritionDelete(date: LocalDate) {
            nutritionDeletes += date
        }

        override suspend fun syncNow(daysBack: Int) = Unit
    }
}
