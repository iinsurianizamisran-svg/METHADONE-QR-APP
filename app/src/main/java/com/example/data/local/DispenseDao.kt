package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DispenseRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DispenseDao {
    @Query("SELECT * FROM dispense_records ORDER BY timestamp DESC")
    fun getAllRecordsFlow(): Flow<List<DispenseRecord>>

    @Query("SELECT * FROM dispense_records WHERE dispenseDate = :date ORDER BY timestamp DESC")
    fun getRecordsByDateFlow(date: String): Flow<List<DispenseRecord>>

    @Query("SELECT * FROM dispense_records WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getRecordsForPatientFlow(patientId: String): Flow<List<DispenseRecord>>

    @Query("SELECT * FROM dispense_records WHERE patientId = :patientId AND dispenseDate = :date LIMIT 1")
    suspend fun getRecordForPatientToday(patientId: String, date: String): DispenseRecord?

    @Query("SELECT COUNT(*) FROM dispense_records WHERE dispenseDate = :date")
    fun getTodayDispensedCountFlow(date: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(doseMg), 0.0) FROM dispense_records WHERE dispenseDate = :date")
    fun getTodayTotalMgFlow(date: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(doseVolumeMl), 0.0) FROM dispense_records WHERE dispenseDate = :date")
    fun getTodayTotalMlFlow(date: String): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DispenseRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<DispenseRecord>)

    @Delete
    suspend fun deleteRecord(record: DispenseRecord)

    @Query("DELETE FROM dispense_records WHERE recordId = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("SELECT * FROM dispense_records WHERE isSynced = 0")
    suspend fun getUnsyncedRecords(): List<DispenseRecord>

    @Query("UPDATE dispense_records SET isSynced = 1 WHERE recordId = :recordId")
    suspend fun markAsSynced(recordId: Long)
}
