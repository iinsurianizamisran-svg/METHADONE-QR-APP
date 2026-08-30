package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dispense_records")
data class DispenseRecord(
    @PrimaryKey(autoGenerate = true)
    val recordId: Long = 0,
    val patientId: String,
    val patientName: String,
    val patientIc: String,
    val dispenseDate: String, // yyyy-MM-dd
    val dispenseTime: String, // HH:mm:ss
    val doseMg: Double,
    val doseVolumeMl: Double,
    val dispenseType: String, // "DOT" or "TAKE_HOME"
    val takeHomeBottlesCount: Int = 0,
    val bottlesReturned: Boolean = true,
    val officerName: String = "Jururawat Kanan (Farmasi)",
    val scanMethod: String = "PENGIMBAS_QR", // "PENGIMBAS_QR", "CARIAN_MANUAL", "KAD_DIGITAL"
    val attendanceStatus: String = "HADIR_DISPENSI", // "HADIR_DISPENSI", "HADIR_JUMPA_DOKTOR", "TIDAK_HADIR"
    val remarks: String = "Ubat diminum sepenuhnya di hadapan petugas (DOT).",
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
)
