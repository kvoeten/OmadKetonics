package com.kazvoeten.omadketonics.data.repository.di

import android.content.Context
import androidx.room.Room
import com.kazvoeten.omadketonics.data.local.OmadDatabase
import com.kazvoeten.omadketonics.data.local.dao.GroceryDao
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
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideRecipeDao(database: OmadDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideWeekPlanDao(database: OmadDatabase): WeekPlanDao = database.weekPlanDao()

    @Provides
    fun provideTrackingDao(database: OmadDatabase): TrackingDao = database.trackingDao()

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
