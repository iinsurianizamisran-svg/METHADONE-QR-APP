package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InventoryItem
import com.example.data.model.InventoryLog
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory WHERE id = 1 LIMIT 1")
    fun getInventoryFlow(): Flow<InventoryItem?>

    @Query("SELECT * FROM inventory WHERE id = 1 LIMIT 1")
    suspend fun getInventory(): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateInventory(item: InventoryItem)

    @Update
    suspend fun updateInventory(item: InventoryItem)

    @Query("SELECT * FROM inventory_logs ORDER BY timestamp DESC")
    fun getAllInventoryLogsFlow(): Flow<List<InventoryLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryLog(log: InventoryLog)

    @Query("DELETE FROM inventory_logs WHERE id = :logId")
    suspend fun deleteInventoryLog(logId: Long)
}
