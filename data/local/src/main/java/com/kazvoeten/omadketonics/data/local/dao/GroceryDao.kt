package com.kazvoeten.omadketonics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kazvoeten.omadketonics.data.local.entity.GroceryCheckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery_checks WHERE week_start = :weekStart")
    fun observeChecklist(weekStart: String): Flow<List<GroceryCheckEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: GroceryCheckEntity)

    @Query("DELETE FROM grocery_checks WHERE week_start = :weekStart AND item_name IN (:itemNames)")
    suspend fun deleteItems(weekStart: String, itemNames: List<String>)

    @Query("DELETE FROM grocery_checks WHERE week_start = :weekStart")
    suspend fun clearWeek(weekStart: String)
}
