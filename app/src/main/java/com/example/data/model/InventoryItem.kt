package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class InventoryItem(
    @PrimaryKey
    val id: Int = 1,
    val medicationName: String = "Methadone Oral Concentrate 5mg/mL",
    val currentStockMl: Double = 4500.0,
    val reorderThresholdMl: Double = 1000.0,
    val unitConcentrationMgPerMl: Double = 5.0,
    val lastRestockDate: String = "2026-02-20",
    val lastRestockMl: Double = 5000.0,
    val batchNumber: String = "MTH-2026-B892",
    val expiryDate: String = "2027-08-31",
    val supplierName: String = "PharmaCare Malaysia / KKM Store",
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
) {
    val currentStockLiters: Double
        get() = currentStockMl / 1000.0

    val reorderThresholdLiters: Double
        get() = reorderThresholdMl / 1000.0

    val isLowStock: Boolean
        get() = currentStockMl <= reorderThresholdMl

    val stockPercentage: Int
        get() {
            val maxCapacityMl = lastRestockMl.coerceAtLeast(reorderThresholdMl * 2.0).coerceAtLeast(1000.0)
            return ((currentStockMl / maxCapacityMl) * 100).toInt().coerceIn(0, 100)
        }
}
