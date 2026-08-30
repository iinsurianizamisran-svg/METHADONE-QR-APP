package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: String, // e.g. "METH-2026-0101"
    val name: String,
    val icNumber: String, // e.g. "820412-10-5543"
    val phoneNumber: String = "012-3456789",
    val currentDoseMg: Double, // e.g. 60.0 mg
    val doseVolumeMl: Double, // calculated at 5mg/mL = 12.0 mL
    val dispenseType: String = "DOT", // "DOT" (Minum di Klinik) or "TAKE_HOME" (Bawa Balik)
    val takeHomeDays: Int = 0, // 0 for DOT, 1 to 7 for Take-Home
    val registrationDate: String, // yyyy-MM-dd
    val clinicLocation: String = "Klinik Kesihatan Cheras",
    val doctorName: String = "Dr. Farah Hanim (Pakar Perubatan Keluarga)",
    val status: String = "AKTIF", // "AKTIF", "GANTUNG", "TAMAT"
    val notes: String = "Patuh rawatan harian. Ujian saringan berkala memuaskan.",
    val lastDispensedDate: String? = null,
    val lastDispensedTime: String? = null,
    val missedDaysStreak: Int = 0
) {
    fun toQrPayload(): String {
        return "METH_QR|$patientId|$icNumber|$name|$currentDoseMg|$dispenseType"
    }
}
