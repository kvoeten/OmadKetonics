package com.kazvoeten.omadketonics.data.local.mapper

import com.kazvoeten.omadketonics.data.local.entity.MealHistoryEntity
import com.kazvoeten.omadketonics.data.local.entity.MoodLogEntity
import com.kazvoeten.omadketonics.data.local.entity.RatingEntity
import com.kazvoeten.omadketonics.data.local.entity.RecipeEntity
import com.kazvoeten.omadketonics.data.local.entity.RecipeIngredientEntity
import com.kazvoeten.omadketonics.data.local.entity.RecipeInstructionEntity
import com.kazvoeten.omadketonics.data.local.entity.WeightLogEntity
import com.kazvoeten.omadketonics.data.local.entity.HealthDailySummaryEntity
import com.kazvoeten.omadketonics.data.local.entity.ManualActivityLogEntity
import com.kazvoeten.omadketonics.data.local.model.RecipeWithDetails
import com.kazvoeten.omadketonics.data.local.model.WeekPlanWithMeals
import com.kazvoeten.omadketonics.model.ActivitySummary
import com.kazvoeten.omadketonics.model.DailyHealthSummary
import com.kazvoeten.omadketonics.model.DailyMood
import com.kazvoeten.omadketonics.model.HealthOutboxStatus
import com.kazvoeten.omadketonics.model.Ingredient
import com.kazvoeten.omadketonics.model.IngredientCategory
import com.kazvoeten.omadketonics.model.IngredientNutrition
import com.kazvoeten.omadketonics.model.ManualActivityLog
import com.kazvoeten.omadketonics.model.ManualActivitySource
import com.kazvoeten.omadketonics.model.MealHistoryEntry
import com.kazvoeten.omadketonics.model.Recipe
import com.kazvoeten.omadketonics.model.SleepSummary
import com.kazvoeten.omadketonics.model.WeekSnapshot
import java.time.LocalDate

fun RecipeWithDetails.toDomain(): Recipe {
    return Recipe(
        id = recipe.id,
        name = recipe.name,
        calories = recipe.calories,
        protein = recipe.protein,
        carbs = recipe.carbs,
        fat = recipe.fat,
        ingredients = ingredients.sortedBy { it.position }.map { it.toDomainIngredient() },
        instructions = instructions.sortedBy { it.stepIndex }.map { it.text },
        icon = recipe.recipeIcon,
        imageUri = recipe.recipeImageUri,
    )
}

fun Recipe.toEntity(): RecipeEntity {
    return RecipeEntity(
        id = id,
        name = name,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        recipeIcon = icon,
        recipeImageUri = imageUri,
    )
}

fun Recipe.toIngredientEntities(): List<RecipeIngredientEntity> {
    return ingredients.mapIndexed { index, ingredient ->
        RecipeIngredientEntity(
            recipeId = id,
            position = index,
            name = ingredient.name,
            category = ingredient.category.name,
            amountGrams = ingredient.amountGrams,
            apiProductCode = ingredient.apiProductCode,
            apiProductName = ingredient.apiProductName,
            apiBrand = ingredient.apiBrand,
            apiNutriScore = ingredient.apiNutriScore,
            nutritionCalories = ingredient.nutrientsPer100g?.calories,
            nutritionProtein = ingredient.nutrientsPer100g?.protein,
            nutritionCarbs = ingredient.nutrientsPer100g?.carbs,
            nutritionFat = ingredient.nutrientsPer100g?.fat,
        )
    }
}

fun Recipe.toInstructionEntities(): List<RecipeInstructionEntity> {
    return instructions.mapIndexed { index, text ->
        RecipeInstructionEntity(
            recipeId = id,
            stepIndex = index,
            text = text,
        )
    }
}

fun RecipeIngredientEntity.toDomainIngredient(): Ingredient {
    val nutrition = if (
        nutritionCalories != null && nutritionProtein != null && nutritionCarbs != null && nutritionFat != null
    ) {
        IngredientNutrition(
            calories = nutritionCalories,
            protein = nutritionProtein,
            carbs = nutritionCarbs,
            fat = nutritionFat,
        )
    } else {
        null
    }
    return Ingredient(
        name = name,
        category = IngredientCategory.entries.firstOrNull { it.name == category } ?: IngredientCategory.Pantry,
        amountGrams = amountGrams,
        apiProductCode = apiProductCode,
        apiProductName = apiProductName,
        apiBrand = apiBrand,
        apiNutriScore = apiNutriScore,
        nutrientsPer100g = nutrition,
    )
}

fun WeekPlanWithMeals.toSnapshot(): WeekSnapshot {
    val ordered = meals.sortedBy { it.position }
    return WeekSnapshot(
        startDate = LocalDate.parse(weekPlan.startDate),
        mealIds = ordered.map { it.recipeId },
        eatenMealIds = ordered.filter { it.isEaten }.map { it.recipeId },
    )
}

fun MealHistoryEntity.toDomain(): MealHistoryEntry {
    return MealHistoryEntry(
        date = LocalDate.parse(date),
        mealId = mealId,
        name = name,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
    )
}

fun MealHistoryEntry.toEntity(): MealHistoryEntity {
    return MealHistoryEntity(
        date = date.toString(),
        mealId = mealId,
        name = name,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
    )
}

fun WeightLogEntity.toDomainEntry(): Pair<LocalDate, Float> = LocalDate.parse(date) to weight
fun MoodLogEntity.toDomainEntry(): Pair<LocalDate, DailyMood>? {
    val mood = DailyMood.entries.firstOrNull { it.name == this.mood } ?: return null
    return LocalDate.parse(date) to mood
}
fun RatingEntity.toDomainEntry(): Pair<String, Int> = recipeId to rating

fun LocalDate.toWeightEntity(value: Float): WeightLogEntity = WeightLogEntity(date = toString(), weight = value)
fun LocalDate.toMoodEntity(mood: DailyMood): MoodLogEntity = MoodLogEntity(date = toString(), mood = mood.name)

fun HealthDailySummaryEntity.toDomain(): DailyHealthSummary {
    return DailyHealthSummary(
        date = LocalDate.parse(date),
        sleep = SleepSummary(
            totalSleepMinutes = sleepTotalMinutes,
            deepSleepMinutes = sleepDeepMinutes,
            remSleepMinutes = sleepRemMinutes,
            lightSleepMinutes = sleepLightMinutes,
            sessionCount = sleepSessionCount,
        ),
        activity = ActivitySummary(
            exerciseMinutes = exerciseMinutes,
            activeCalories = activeCalories,
            sessionCount = activitySessionCount,
            highIntensitySessions = highIntensitySessions,
            moderateIntensitySessions = moderateIntensitySessions,
            lowIntensitySessions = lowIntensitySessions,
        ),
        source = source,
    )
}

fun DailyHealthSummary.toEntity(updatedAtEpoch: Long): HealthDailySummaryEntity {
    return HealthDailySummaryEntity(
        date = date.toString(),
        sleepTotalMinutes = sleep.totalSleepMinutes,
        sleepDeepMinutes = sleep.deepSleepMinutes,
        sleepRemMinutes = sleep.remSleepMinutes,
        sleepLightMinutes = sleep.lightSleepMinutes,
        sleepSessionCount = sleep.sessionCount,
        exerciseMinutes = activity.exerciseMinutes,
        activeCalories = activity.activeCalories,
        activitySessionCount = activity.sessionCount,
        highIntensitySessions = activity.highIntensitySessions,
        moderateIntensitySessions = activity.moderateIntensitySessions,
        lowIntensitySessions = activity.lowIntensitySessions,
        source = source,
        updatedAtEpoch = updatedAtEpoch,
    )
}

fun ManualActivityLogEntity.toDomain(): ManualActivityLog {
    return ManualActivityLog(
        id = id,
        startTimeEpochMillis = startTimeEpochMillis,
        endTimeEpochMillis = endTimeEpochMillis,
        activityType = activityType,
        exertion = exertion,
        calories = calories,
        source = ManualActivitySource.entries.firstOrNull { it.name == source } ?: ManualActivitySource.AppManual,
        outboxStatus = HealthOutboxStatus.entries.firstOrNull { it.name == outboxStatus } ?: HealthOutboxStatus.Pending,
        healthClientRecordId = healthClientRecordId,
        createdAtEpochMillis = createdAtEpochMillis,
        syncedAtEpochMillis = syncedAtEpochMillis,
    )
}

fun ManualActivityLog.toEntity(notes: String? = null): ManualActivityLogEntity {
    return ManualActivityLogEntity(
        id = id,
        startTimeEpochMillis = startTimeEpochMillis,
        endTimeEpochMillis = endTimeEpochMillis,
        activityType = activityType,
        exertion = exertion,
        calories = calories,
        source = source.name,
        outboxStatus = outboxStatus.name,
        healthClientRecordId = healthClientRecordId,
        notes = notes,
        createdAtEpochMillis = createdAtEpochMillis,
        syncedAtEpochMillis = syncedAtEpochMillis,
    )
}
