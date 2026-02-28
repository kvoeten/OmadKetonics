package com.kazvoeten.omadketonics.data.repository.impl

import androidx.room.withTransaction
import com.kazvoeten.omadketonics.data.local.OmadDatabase
import com.kazvoeten.omadketonics.data.local.dao.WeekPlanDao
import com.kazvoeten.omadketonics.data.local.entity.WeekPlanEntity
import com.kazvoeten.omadketonics.data.local.entity.WeekPlanMealEntity
import com.kazvoeten.omadketonics.data.local.mapper.toSnapshot
import com.kazvoeten.omadketonics.domain.repository.WeekPlanRepository
import com.kazvoeten.omadketonics.model.WeekSnapshot
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class WeekPlanRepositoryImpl @Inject constructor(
    private val database: OmadDatabase,
    private val weekPlanDao: WeekPlanDao,
) : WeekPlanRepository {
    override fun observeWeeksDescending(): Flow<List<WeekSnapshot>> {
        return weekPlanDao.observeWeeksWithMealsDescending().map { items ->
            items.map { it.toSnapshot() }
        }
    }

    override suspend fun getCurrentWeek(): WeekSnapshot? {
        return weekPlanDao.getCurrentWeekWithMeals()?.toSnapshot()
    }

    override suspend fun ensureCurrentWeekExists(mealIds: List<String>, date: LocalDate) {
        val current = weekPlanDao.getCurrentWeekWithMeals()
        if (current != null) return

        generateNewCurrentWeek(date, mealIds)
    }

    override suspend fun generateNewCurrentWeek(startDate: LocalDate, mealIds: List<String>) {
        val weekStart = startDate.toString()
        database.withTransaction {
            weekPlanDao.clearCurrentWeekFlag()
            weekPlanDao.upsertWeekPlan(
                WeekPlanEntity(
                    startDate = weekStart,
                    isCurrent = true,
                ),
            )
            weekPlanDao.deleteMealsForWeek(weekStart)
            weekPlanDao.upsertWeekMeals(
                mealIds.mapIndexed { index, recipeId ->
                    WeekPlanMealEntity(
                        weekStartDate = weekStart,
                        recipeId = recipeId,
                        position = index,
                        isEaten = false,
                    )
                },
            )
        }
    }

    override suspend fun addMealToCurrentWeek(recipeId: String) {
        val current = weekPlanDao.getCurrentWeekWithMeals() ?: return
        val weekStart = current.weekPlan.startDate
        val existing = weekPlanDao.getWeekMeal(weekStart, recipeId)
        if (existing != null) return

        val nextPosition = (weekPlanDao.getLastPosition(weekStart) ?: -1) + 1
        weekPlanDao.upsertWeekMeal(
            WeekPlanMealEntity(
                weekStartDate = weekStart,
                recipeId = recipeId,
                position = nextPosition,
                isEaten = false,
            ),
        )
    }

    override suspend fun setMealEatenInCurrentWeek(recipeId: String, eaten: Boolean) {
        val current = weekPlanDao.getCurrentWeekWithMeals() ?: return
        weekPlanDao.setMealEaten(current.weekPlan.startDate, recipeId, eaten)
    }
}
