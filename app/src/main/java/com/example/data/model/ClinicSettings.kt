package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clinic_settings")
data class ClinicSettings(
    @PrimaryKey
    val id: Int = 1,
    val clinicName: String = "e-Methadone PKD Kluang",
    val isSetupCompleted: Boolean = false,
    val activePatientsCount: Int = 45,
    val newCasesCount: Int = 5,
    val defaultersCount: Int = 2,
    val restartCount: Int = 1,
    val transferInCount: Int = 3,
    val transferOutCount: Int = 1,
    val deathCount: Int = 0,
    val terminatedCount: Int = 1,
    val initialBatchNumber: String = "MTH-2026-B892",
    val initialExpiryDate: String = "2027-12-31",
    val initialStockLiters: Double = 5.0,
    val initialStrength: String = "5 mg / 1 ml",
    val ndmaRegNo: String = "NDMA-KPM-9281A",
    val ndmaStatus: String = "Berdaftar / Aktif",
    val autoBackupPath: String = "/sdcard/eMethadone_Backup",
    val lastBackupDate: String? = null
)
