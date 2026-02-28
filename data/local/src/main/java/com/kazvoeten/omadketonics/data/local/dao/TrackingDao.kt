package com.kazvoeten.omadketonics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kazvoeten.omadketonics.data.local.entity.MealHistoryEntity
import com.kazvoeten.omadketonics.data.local.entity.MoodLogEntity
import com.kazvoeten.omadketonics.data.local.entity.RatingEntity
import com.kazvoeten.omadketonics.data.local.entity.WeightLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackingDao {
    @Query("SELECT * FROM meal_history ORDER BY date DESC")
    fun observeMealHistory(): Flow<List<MealHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMealHistory(item: MealHistoryEntity)

    @Query("DELETE FROM meal_history WHERE date = :date")
    suspend fun deleteMealHistoryByDate(date: String)

    @Query("SELECT * FROM weight_logs")
    fun observeWeightLogs(): Flow<List<WeightLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeight(item: WeightLogEntity)

    @Query("SELECT * FROM mood_logs")
    fun observeMoodLogs(): Flow<List<MoodLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMood(item: MoodLogEntity)

    @Query("DELETE FROM mood_logs WHERE date = :date")
    suspend fun deleteMoodByDate(date: String)

    @Query("SELECT * FROM ratings")
    fun observeRatings(): Flow<List<RatingEntity>>

    @Query("SELECT * FROM ratings")
    suspend fun getRatings(): List<RatingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRating(item: RatingEntity)
}
