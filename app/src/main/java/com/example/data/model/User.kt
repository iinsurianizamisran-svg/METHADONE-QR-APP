package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val role: String, // Farmasi, Doktor, AMO, Pentadbir
    val icOrStaffId: String,
    val email: String? = null,
    val createdDate: String
)

object UserRoles {
    const val ADMIN = "Pentadbir"
    const val PHARMACY = "Farmasi"
    const val DOCTOR = "Doktor"
    const val AMO = "AMO"

    val ALL_ROLES = listOf(PHARMACY, DOCTOR, AMO)
}
