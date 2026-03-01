package com.kazvoeten.omadketonics.data.repository.di

import android.content.Context
import androidx.room.Room
import com.kazvoeten.omadketonics.data.local.OmadDatabase
import com.kazvoeten.omadketonics.data.local.MIGRATION_1_2
import com.kazvoeten.omadketonics.data.local.MIGRATION_2_3
import com.kazvoeten.omadketonics.data.local.MIGRATION_3_4
import com.kazvoeten.omadketonics.data.local.dao.GroceryDao
import com.kazvoeten.omadketonics.data.local.dao.HealthDao
import com.kazvoeten.omadketonics.data.local.dao.RecipeDao
import com.kazvoeten.omadketonics.data.local.dao.SearchCacheDao
import com.kazvoeten.omadketonics.data.local.dao.TrackingDao
import com.kazvoeten.omadketonics.data.local.dao.WeekPlanDao
import com.kazvoeten.omadketonics.data.local.store.UserPreferencesStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDataModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): OmadDatabase {
        return Room.databaseBuilder(
            context,
            OmadDatabase::class.java,
            "omad.db",
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideRecipeDao(database: OmadDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideWeekPlanDao(database: OmadDatabase): WeekPlanDao = database.weekPlanDao()

    @Provides
    fun provideTrackingDao(database: OmadDatabase): TrackingDao = database.trackingDao()

    @Provides
    fun provideHealthDao(database: OmadDatabase): HealthDao = database.healthDao()

    @Provides
    fun provideGroceryDao(database: OmadDatabase): GroceryDao = database.groceryDao()

    @Provides
    fun provideSearchCacheDao(database: OmadDatabase): SearchCacheDao = database.searchCacheDao()

    @Provides
    @Singleton
    fun provideUserPreferencesStore(
        @ApplicationContext context: Context,
    ): UserPreferencesStore = UserPreferencesStore(context)
}
