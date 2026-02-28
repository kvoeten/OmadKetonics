package com.kazvoeten.omadketonics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kazvoeten.omadketonics.data.local.entity.IngredientSearchCacheEntity

@Dao
interface SearchCacheDao {
    @Query("SELECT * FROM ingredient_search_cache WHERE query = :query ORDER BY created_at_epoch DESC")
    suspend fun getQueryCache(query: String): List<IngredientSearchCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<IngredientSearchCacheEntity>)

    @Query("DELETE FROM ingredient_search_cache WHERE query = :query")
    suspend fun clearQuery(query: String)

    @Query("DELETE FROM ingredient_search_cache WHERE created_at_epoch < :minEpoch")
    suspend fun clearExpired(minEpoch: Long)
}
