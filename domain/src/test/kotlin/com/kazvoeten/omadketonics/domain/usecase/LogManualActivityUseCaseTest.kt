package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.core.common.DateProvider
import com.kazvoeten.omadketonics.domain.repository.HealthRepository
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import com.kazvoeten.omadketonics.model.DailyHealthSummary
import com.kazvoeten.omadketonics.model.DailyMood
import com.kazvoeten.omadketonics.model.HealthConnectionState
import com.kazvoeten.omadketonics.model.ManualActivityInput
import com.kazvoeten.omadketonics.model.ManualActivityLog
import com.kazvoeten.omadketonics.model.MealHistoryEntry
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogManualActivityUseCaseTest {
    @Test
    fun `estimates calories and queues activity`() = runBlocking {
        val health = FakeHealthRepository()
        val tracking = FakeTrackingRepository(weights = mapOf(LocalDate.now() to 82f))
        val useCase = LogManualActivityUseCase(
            healthRepository = health,
            trackingRepository = tracking,
            dateProvider = DateProvider(),
        )

        val end = 1_000_000L
        val start = end - 45L * 60_000L
        val result = useCase(
            ManualActivityInput(
                startTimeEpochMillis = start,
                endTimeEpochMillis = end,
                activityType = "Walking",
                exertion = 6,
            ),
        )

        assertTrue(result.success)
        assertTrue(result.calories > 0)
        assertEquals(1, health.queuedActivities.size)
    }

    @Test
    fun `manual calorie override takes precedence`() = runBlocking {
        val health = FakeHealthRepository()
        val tracking = FakeTrackingRepository(weights = emptyMap())
        val useCase = LogManualActivityUseCase(
            healthRepository = health,
            trackingRepository = tracking,
            dateProvider = DateProvider(),
        )

        val end = 2_000_000L
        val start = end - 30L * 60_000L
        val result = useCase(
            ManualActivityInput(
                startTimeEpochMillis = start,
                endTimeEpochMillis = end,
                activityType = "HIIT",
                exertion = 8,
                caloriesOverride = 333,
            ),
        )

        assertTrue(result.success)
        assertEquals(333, result.calories)
        assertEquals(333, health.queuedActivities.first().estimatedCalories)
    }

    private data class QueuedActivity(
        val input: ManualActivityInput,
        val estimatedCalories: Int,
        val clientRecordId: String,
        val source: String,
    )

    private class FakeHealthRepository : HealthRepository {
        val queuedActivities = mutableListOf<QueuedActivity>()

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
        ) {
            queuedActivities += QueuedActivity(input, estimatedCalories, clientRecordId, source)
        }

        override suspend fun queueNutritionUpsert(
            date: LocalDate,
            mealName: String,
            calories: Int,
            protein: Int,
            carbs: Int,
            fat: Int,
        ) = Unit

        override suspend fun queueNutritionDelete(date: LocalDate) = Unit

        override suspend fun syncNow(daysBack: Int) = Unit
    }

    private class FakeTrackingRepository(
        private val weights: Map<LocalDate, Float>,
    ) : TrackingRepository {
        override fun observeMealHistory(): Flow<List<MealHistoryEntry>> = flowOf(emptyList())

        override fun observeMealHistoryBetween(
            startDate: LocalDate,
            endDate: LocalDate,
        ): Flow<List<MealHistoryEntry>> = flowOf(emptyList())

        override fun observeWeights(): Flow<Map<LocalDate, Float>> = flowOf(weights)

        override fun observeMoods(): Flow<Map<LocalDate, DailyMood>> = flowOf(emptyMap())

        override fun observeRatings(): Flow<Map<String, Int>> = flowOf(emptyMap())

        override suspend fun upsertMealHistory(entry: MealHistoryEntry) = Unit

        override suspend fun removeMealHistoryForDate(date: LocalDate) = Unit

        override suspend fun setWeight(date: LocalDate, value: Float) = Unit

        override suspend fun setMood(date: LocalDate, mood: DailyMood?) = Unit

        override suspend fun setRating(recipeId: String, rating: Int) = Unit

        override suspend fun getRatings(): Map<String, Int> = emptyMap()
    }
}
