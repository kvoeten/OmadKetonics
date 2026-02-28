package com.kazvoeten.omadketonics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kazvoeten.omadketonics.data.local.entity.WeekPlanEntity
import com.kazvoeten.omadketonics.data.local.entity.WeekPlanMealEntity
import com.kazvoeten.omadketonics.data.local.model.WeekPlanWithMeals
import kotlinx.coroutines.flow.Flow

@Dao
interface WeekPlanDao {
    @Transaction
    @Query("SELECT * FROM week_plans ORDER BY start_date DESC")
    fun observeWeeksWithMealsDescending(): Flow<List<WeekPlanWithMeals>>

    @Transaction
    @Query("SELECT * FROM week_plans WHERE is_current = 1 LIMIT 1")
    suspend fun getCurrentWeekWithMeals(): WeekPlanWithMeals?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeekPlan(item: WeekPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeekMeals(items: List<WeekPlanMealEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeekMeal(item: WeekPlanMealEntity)

    @Query("SELECT * FROM week_plan_meals WHERE week_start_date = :weekStartDate AND recipe_id = :recipeId LIMIT 1")
    suspend fun getWeekMeal(weekStartDate: String, recipeId: String): WeekPlanMealEntity?

    @Query("SELECT MAX(position) FROM week_plan_meals WHERE week_start_date = :weekStartDate")
    suspend fun getLastPosition(weekStartDate: String): Int?

    @Query("UPDATE week_plans SET is_current = 0")
    suspend fun clearCurrentWeekFlag()

    @Query("DELETE FROM week_plan_meals WHERE week_start_date = :weekStartDate")
    suspend fun deleteMealsForWeek(weekStartDate: String)

    @Query("UPDATE week_plan_meals SET is_eaten = :isEaten WHERE week_start_date = :weekStartDate AND recipe_id = :recipeId")
    suspend fun setMealEaten(weekStartDate: String, recipeId: String, isEaten: Boolean)
}
