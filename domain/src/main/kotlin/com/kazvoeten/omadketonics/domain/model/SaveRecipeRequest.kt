package com.kazvoeten.omadketonics.domain.model

import com.kazvoeten.omadketonics.model.Ingredient

data class SaveRecipeRequest(
    val existingId: String?,
    val name: String,
    val icon: String,
    val ingredients: List<Ingredient>,
    val instructionsInput: String,
)
