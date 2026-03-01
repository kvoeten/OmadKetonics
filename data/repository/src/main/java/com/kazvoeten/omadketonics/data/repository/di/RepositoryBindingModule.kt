package com.kazvoeten.omadketonics.data.repository.di

import com.kazvoeten.omadketonics.data.repository.impl.GroceryRepositoryImpl
import com.kazvoeten.omadketonics.data.repository.impl.HealthRepositoryImpl
import com.kazvoeten.omadketonics.data.repository.impl.IngredientSearchRepositoryImpl
import com.kazvoeten.omadketonics.data.repository.impl.RecipeRepositoryImpl
import com.kazvoeten.omadketonics.data.repository.impl.TrackingRepositoryImpl
import com.kazvoeten.omadketonics.data.repository.impl.UserPreferencesRepositoryImpl
import com.kazvoeten.omadketonics.data.repository.impl.WeekPlanRepositoryImpl
import com.kazvoeten.omadketonics.data.repository.policy.DefaultIngredientCategoryInferencePolicy
import com.kazvoeten.omadketonics.data.repository.policy.DefaultProductRankingPolicy
import com.kazvoeten.omadketonics.data.repository.policy.DefaultWeekMealSelectionPolicy
import com.kazvoeten.omadketonics.domain.policy.IngredientCategoryInferencePolicy
import com.kazvoeten.omadketonics.domain.policy.ProductRankingPolicy
import com.kazvoeten.omadketonics.domain.policy.WeekMealSelectionPolicy
import com.kazvoeten.omadketonics.domain.repository.GroceryRepository
import com.kazvoeten.omadketonics.domain.repository.HealthRepository
import com.kazvoeten.omadketonics.domain.repository.IngredientSearchRepository
import com.kazvoeten.omadketonics.domain.repository.RecipeRepository
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import com.kazvoeten.omadketonics.domain.repository.UserPreferencesRepository
import com.kazvoeten.omadketonics.domain.repository.WeekPlanRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingModule {
    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindWeekPlanRepository(impl: WeekPlanRepositoryImpl): WeekPlanRepository

    @Binds
    @Singleton
    abstract fun bindTrackingRepository(impl: TrackingRepositoryImpl): TrackingRepository

    @Binds
    @Singleton
    abstract fun bindHealthRepository(impl: HealthRepositoryImpl): HealthRepository

    @Binds
    @Singleton
    abstract fun bindGroceryRepository(impl: GroceryRepositoryImpl): GroceryRepository

    @Binds
    @Singleton
    abstract fun bindIngredientSearchRepository(impl: IngredientSearchRepositoryImpl): IngredientSearchRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindWeekMealSelectionPolicy(impl: DefaultWeekMealSelectionPolicy): WeekMealSelectionPolicy

    @Binds
    @Singleton
    abstract fun bindIngredientCategoryPolicy(impl: DefaultIngredientCategoryInferencePolicy): IngredientCategoryInferencePolicy

    @Binds
    @Singleton
    abstract fun bindProductRankingPolicy(impl: DefaultProductRankingPolicy): ProductRankingPolicy
}
