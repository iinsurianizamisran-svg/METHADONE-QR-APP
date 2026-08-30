package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ClinicSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface ClinicSettingsDao {
    @Query("SELECT * FROM clinic_settings WHERE id = 1 LIMIT 1")
    fun getClinicSettingsFlow(): Flow<ClinicSettings?>

    @Query("SELECT * FROM clinic_settings WHERE id = 1 LIMIT 1")
    suspend fun getClinicSettings(): ClinicSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveClinicSettings(settings: ClinicSettings)
}
