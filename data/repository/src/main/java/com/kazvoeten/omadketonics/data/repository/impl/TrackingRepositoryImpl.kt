package com.kazvoeten.omadketonics.data.repository.impl

import com.kazvoeten.omadketonics.data.local.dao.TrackingDao
import com.kazvoeten.omadketonics.data.local.entity.RatingEntity
import com.kazvoeten.omadketonics.data.local.mapper.toDomain
import com.kazvoeten.omadketonics.data.local.mapper.toDomainEntry
import com.kazvoeten.omadketonics.data.local.mapper.toEntity
import com.kazvoeten.omadketonics.data.local.mapper.toMoodEntity
import com.kazvoeten.omadketonics.data.local.mapper.toWeightEntity
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import com.kazvoeten.omadketonics.model.DailyMood
import com.kazvoeten.omadketonics.model.MealHistoryEntry
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class TrackingRepositoryImpl @Inject constructor(
    private val trackingDao: TrackingDao,
) : TrackingRepository {
    override fun observeMealHistory(): Flow<List<MealHistoryEntry>> {
        return trackingDao.observeMealHistory().map { list -> list.map { it.toDomain() } }
    }

    override fun observeWeights(): Flow<Map<LocalDate, Float>> {
        return trackingDao.observeWeightLogs().map { list ->
            list.map { it.toDomainEntry() }.toMap()
        }
    }

    override fun observeMoods(): Flow<Map<LocalDate, DailyMood>> {
        return trackingDao.observeMoodLogs().map { list ->
            list.mapNotNull { it.toDomainEntry() }.toMap()
        }
    }

    override fun observeRatings(): Flow<Map<String, Int>> {
        return trackingDao.observeRatings().map { list ->
            list.map { it.toDomainEntry() }.toMap()
        }
    }

    override suspend fun upsertMealHistory(entry: MealHistoryEntry) {
        trackingDao.upsertMealHistory(entry.toEntity())
    }

    override suspend fun removeMealHistoryForDate(date: LocalDate) {
        trackingDao.deleteMealHistoryByDate(date.toString())
    }

    override suspend fun setWeight(date: LocalDate, value: Float) {
        trackingDao.upsertWeight(date.toWeightEntity(value))
    }

    override suspend fun setMood(date: LocalDate, mood: DailyMood?) {
        if (mood == null) {
            trackingDao.deleteMoodByDate(date.toString())
        } else {
            trackingDao.upsertMood(date.toMoodEntity(mood))
        }
    }

    override suspend fun setRating(recipeId: String, rating: Int) {
        trackingDao.upsertRating(RatingEntity(recipeId = recipeId, rating = rating.coerceIn(0, 5)))
    }

    override suspend fun getRatings(): Map<String, Int> {
        return trackingDao.getRatings().associate { it.recipeId to it.rating }
    }
}
