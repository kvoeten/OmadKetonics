package com.kazvoeten.omadketonics.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kazvoeten.omadketonics.data.local.dao.GroceryDao
import com.kazvoeten.omadketonics.data.local.dao.HealthDao
import com.kazvoeten.omadketonics.data.local.dao.RecipeDao
import com.kazvoeten.omadketonics.data.local.dao.SearchCacheDao
import com.kazvoeten.omadketonics.data.local.dao.TrackingDao
import com.kazvoeten.omadketonics.data.local.dao.WeekPlanDao
import com.kazvoeten.omadketonics.data.local.entity.GroceryCheckEntity
import com.kazvoeten.omadketonics.data.local.entity.HealthDailySummaryEntity
import com.kazvoeten.omadketonics.data.local.entity.HealthOutboxEntity
import com.kazvoeten.omadketonics.data.local.entity.HealthSyncStateEntity
import com.kazvoeten.omadketonics.data.local.entity.IngredientSearchCacheEntity
import com.kazvoeten.omadketonics.data.local.entity.MealHistoryEntity
import com.kazvoeten.omadketonics.data.local.entity.MoodLogEntity
import com.kazvoeten.omadketonics.data.local.entity.ManualActivityLogEntity
import com.kazvoeten.omadketonics.data.local.entity.RatingEntity
import com.kazvoeten.omadketonics.data.local.entity.RecipeEntity
import com.kazvoeten.omadketonics.data.local.entity.RecipeIngredientEntity
import com.kazvoeten.omadketonics.data.local.entity.RecipeInstructionEntity
import com.kazvoeten.omadketonics.data.local.entity.WeekPlanEntity
import com.kazvoeten.omadketonics.data.local.entity.WeekPlanMealEntity
import com.kazvoeten.omadketonics.data.local.entity.WeightLogEntity

@Database(
    entities = [
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        RecipeInstructionEntity::class,
        WeekPlanEntity::class,
        WeekPlanMealEntity::class,
        MealHistoryEntity::class,
        WeightLogEntity::class,
        MoodLogEntity::class,
        RatingEntity::class,
        GroceryCheckEntity::class,
        IngredientSearchCacheEntity::class,
        HealthDailySummaryEntity::class,
        ManualActivityLogEntity::class,
        HealthOutboxEntity::class,
        HealthSyncStateEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class OmadDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun weekPlanDao(): WeekPlanDao
    abstract fun trackingDao(): TrackingDao
    abstract fun healthDao(): HealthDao
    abstract fun groceryDao(): GroceryDao
    abstract fun searchCacheDao(): SearchCacheDao
}
