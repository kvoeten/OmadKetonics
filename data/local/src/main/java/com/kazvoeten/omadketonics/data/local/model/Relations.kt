package com.kazvoeten.omadketonics.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.kazvoeten.omadketonics.data.local.entity.RecipeEntity
import com.kazvoeten.omadketonics.data.local.entity.RecipeIngredientEntity
import com.kazvoeten.omadketonics.data.local.entity.RecipeInstructionEntity
import com.kazvoeten.omadketonics.data.local.entity.WeekPlanEntity
import com.kazvoeten.omadketonics.data.local.entity.WeekPlanMealEntity

data class RecipeWithDetails(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id",
    )
    val ingredients: List<RecipeIngredientEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id",
    )
    val instructions: List<RecipeInstructionEntity>,
)

data class WeekPlanWithMeals(
    @Embedded val weekPlan: WeekPlanEntity,
    @Relation(
        parentColumn = "start_date",
        entityColumn = "week_start_date",
    )
    val meals: List<WeekPlanMealEntity>,
)
