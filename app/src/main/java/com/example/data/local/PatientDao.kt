package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Patient
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY name ASC")
    fun getAllPatientsFlow(): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE patientId = :patientId LIMIT 1")
    suspend fun getPatientById(patientId: String): Patient?

    @Query("SELECT * FROM patients WHERE icNumber = :icNumber LIMIT 1")
    suspend fun getPatientByIc(icNumber: String): Patient?

    @Query("SELECT * FROM patients WHERE name LIKE '%' || :query || '%' OR icNumber LIKE '%' || :query || '%' OR patientId LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchPatientsFlow(query: String): Flow<List<Patient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatients(patients: List<Patient>)

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int

    @Query("UPDATE patients SET lastDispensedDate = :date, lastDispensedTime = :time WHERE patientId = :patientId")
    suspend fun updateLastDispensed(patientId: String, date: String, time: String)
}
