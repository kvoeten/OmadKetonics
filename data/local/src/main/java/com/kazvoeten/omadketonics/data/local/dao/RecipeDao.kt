package com.kazvoeten.omadketonics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kazvoeten.omadketonics.data.local.entity.RecipeEntity
import com.kazvoeten.omadketonics.data.local.entity.RecipeIngredientEntity
import com.kazvoeten.omadketonics.data.local.entity.RecipeInstructionEntity
import com.kazvoeten.omadketonics.data.local.model.RecipeWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Transaction
    @Query("SELECT * FROM recipes ORDER BY name")
    fun observeRecipesWithDetails(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY name")
    suspend fun getRecipesWithDetails(): List<RecipeWithDetails>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId LIMIT 1")
    suspend fun getRecipeWithDetails(recipeId: String): RecipeWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertIngredients(items: List<RecipeIngredientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInstructions(items: List<RecipeInstructionEntity>)

    @Query("DELETE FROM recipe_ingredients WHERE recipe_id = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: String)

    @Query("DELETE FROM recipe_instructions WHERE recipe_id = :recipeId")
    suspend fun deleteInstructionsForRecipe(recipeId: String)

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun countRecipes(): Int

    @Query("SELECT id FROM recipes ORDER BY name")
    suspend fun getAllRecipeIds(): List<String>
}
