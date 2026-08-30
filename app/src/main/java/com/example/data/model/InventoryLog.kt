package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_logs")
data class InventoryLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val time: String,
    val actionType: String, // "TAMBAH_STOK", "DISPENSI_AUTOMATIK", "PELARASAN_MANUAL", "KEMASKINI_HAD"
    val volumeChangeMl: Double,
    val remainingStockMl: Double,
    val batchNumber: String,
    val officerName: String,
    val notes: String,
    val timestamp: Long = System.currentTimeMillis()
)
