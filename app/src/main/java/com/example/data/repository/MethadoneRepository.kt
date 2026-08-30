package com.example.data.repository

import com.example.data.local.ClinicSettingsDao
import com.example.data.local.DispenseDao
import com.example.data.local.InventoryDao
import com.example.data.local.PatientDao
import com.example.data.local.UserDao
import com.example.data.model.ClinicSettings
import com.example.data.model.DispenseRecord
import com.example.data.model.InventoryItem
import com.example.data.model.InventoryLog
import com.example.data.model.Patient
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Locale

class MethadoneRepository(
    private val patientDao: PatientDao,
    private val dispenseDao: DispenseDao,
    private val inventoryDao: InventoryDao,
    private val userDao: UserDao,
    private val clinicSettingsDao: ClinicSettingsDao
) {
    val allPatients: Flow<List<Patient>> = patientDao.getAllPatientsFlow()
    val allDispenseRecords: Flow<List<DispenseRecord>> = dispenseDao.getAllRecordsFlow()
    val inventoryItem: Flow<InventoryItem?> = inventoryDao.getInventoryFlow()
    val inventoryLogs: Flow<List<InventoryLog>> = inventoryDao.getAllInventoryLogsFlow()
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    val clinicSettings: Flow<ClinicSettings?> = clinicSettingsDao.getClinicSettingsFlow()

    suspend fun getClinicSettings(): ClinicSettings? {
        return clinicSettingsDao.getClinicSettings()
    }

    suspend fun saveClinicSettings(settings: ClinicSettings) {
        clinicSettingsDao.saveClinicSettings(settings)
        if (settings.isSetupCompleted) {
            val stockMl = (settings.initialStockLiters * 1000.0).coerceAtLeast(100.0)
            val currentInv = inventoryDao.getInventory() ?: InventoryItem()
            val updatedInv = currentInv.copy(
                medicationName = "Methadone Oral Concentrate ${settings.initialStrength}",
                currentStockMl = stockMl,
                lastRestockMl = stockMl,
                batchNumber = settings.initialBatchNumber.ifBlank { currentInv.batchNumber },
                expiryDate = settings.initialExpiryDate.ifBlank { currentInv.expiryDate },
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
            inventoryDao.insertOrUpdateInventory(updatedInv)

            val log = InventoryLog(
                date = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                time = SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                actionType = "TAMBAH_STOK",
                volumeChangeMl = stockMl,
                remainingStockMl = stockMl,
                batchNumber = settings.initialBatchNumber,
                officerName = "Pentadbir Klinik",
                notes = "Tetapan Permulaan Stok Klinik: ${settings.initialStockLiters} L (${stockMl.toInt()} mL), Batch: ${settings.initialBatchNumber}, Expiry: ${settings.initialExpiryDate}, Kekuatan: ${settings.initialStrength}",
                timestamp = System.currentTimeMillis()
            )
            inventoryDao.insertInventoryLog(log)
        }
    }

    suspend fun deleteInventoryLog(logId: Long) {
        inventoryDao.deleteInventoryLog(logId)
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun getUserByIcOrStaffId(icOrStaffId: String): User? {
        return userDao.getUserByIcOrStaffId(icOrStaffId)
    }

    suspend fun findUserForRecovery(username: String, icOrStaffId: String): User? {
        return userDao.findUserForRecovery(username, icOrStaffId)
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun updatePassword(username: String, newPassword: String) {
        userDao.updatePassword(username, newPassword)
    }

    fun searchPatients(query: String): Flow<List<Patient>> {
        return if (query.isBlank()) {
            patientDao.getAllPatientsFlow()
        } else {
            patientDao.searchPatientsFlow(query.trim())
        }
    }

    fun getRecordsByDate(date: String): Flow<List<DispenseRecord>> {
        return dispenseDao.getRecordsByDateFlow(date)
    }

    fun getRecordsForPatient(patientId: String): Flow<List<DispenseRecord>> {
        return dispenseDao.getRecordsForPatientFlow(patientId)
    }

    fun getTodayDispensedCount(date: String): Flow<Int> {
        return dispenseDao.getTodayDispensedCountFlow(date)
    }

    fun getTodayTotalMg(date: String): Flow<Double> {
        return dispenseDao.getTodayTotalMgFlow(date)
    }

    fun getTodayTotalMl(date: String): Flow<Double> {
        return dispenseDao.getTodayTotalMlFlow(date)
    }

    suspend fun getPatientById(patientId: String): Patient? {
        return patientDao.getPatientById(patientId)
    }

    suspend fun getPatientByIc(icNumber: String): Patient? {
        return patientDao.getPatientByIc(icNumber)
    }

    suspend fun getRecordForPatientToday(patientId: String, date: String): DispenseRecord? {
        return dispenseDao.getRecordForPatientToday(patientId, date)
    }

    suspend fun insertPatient(patient: Patient): Long {
        return patientDao.insertPatient(patient)
    }

    suspend fun updatePatient(patient: Patient) {
        patientDao.updatePatient(patient)
    }

    suspend fun deletePatient(patient: Patient) {
        patientDao.deletePatient(patient)
    }

    suspend fun recordDispense(record: DispenseRecord) {
        dispenseDao.insertRecord(record)
        patientDao.updateLastDispensed(
            patientId = record.patientId,
            date = record.dispenseDate,
            time = record.dispenseTime
        )

        // Deduct from stock automatically
        val totalVolumeDispensedMl = if (record.dispenseType == "TAKE_HOME" && record.takeHomeBottlesCount > 0) {
            record.doseVolumeMl * (record.takeHomeBottlesCount + 1)
        } else {
            record.doseVolumeMl
        }

        val currentInv = inventoryDao.getInventory() ?: InventoryItem()
        val newStock = (currentInv.currentStockMl - totalVolumeDispensedMl).coerceAtLeast(0.0)
        val updatedInv = currentInv.copy(
            currentStockMl = newStock,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        inventoryDao.insertOrUpdateInventory(updatedInv)

        val log = InventoryLog(
            date = record.dispenseDate,
            time = record.dispenseTime,
            actionType = "DISPENSI_AUTOMATIK",
            volumeChangeMl = -totalVolumeDispensedMl,
            remainingStockMl = newStock,
            batchNumber = currentInv.batchNumber,
            officerName = record.officerName,
            notes = "Dispensasi ${record.patientName} (${record.patientId}): ${totalVolumeDispensedMl} mL (${record.dispenseType})",
            timestamp = System.currentTimeMillis()
        )
        inventoryDao.insertInventoryLog(log)
    }

    suspend fun updateReorderThreshold(newThresholdMl: Double, officerName: String, date: String, time: String) {
        val currentInv = inventoryDao.getInventory() ?: InventoryItem()
        val updatedInv = currentInv.copy(
            reorderThresholdMl = newThresholdMl,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        inventoryDao.insertOrUpdateInventory(updatedInv)

        val log = InventoryLog(
            date = date,
            time = time,
            actionType = "KEMASKINI_HAD",
            volumeChangeMl = 0.0,
            remainingStockMl = currentInv.currentStockMl,
            batchNumber = currentInv.batchNumber,
            officerName = officerName,
            notes = "Kemaskini Had Pesanan Semula (Reorder Threshold) kepada ${newThresholdMl.toInt()} mL (${newThresholdMl/1000.0} L).",
            timestamp = System.currentTimeMillis()
        )
        inventoryDao.insertInventoryLog(log)
    }

    suspend fun restockInventory(
        addMl: Double,
        batchNumber: String,
        expiryDate: String,
        supplierName: String,
        officerName: String,
        date: String,
        time: String,
        notes: String
    ) {
        val currentInv = inventoryDao.getInventory() ?: InventoryItem()
        val newStock = currentInv.currentStockMl + addMl
        val updatedInv = currentInv.copy(
            currentStockMl = newStock,
            lastRestockMl = addMl,
            lastRestockDate = date,
            batchNumber = batchNumber.ifBlank { currentInv.batchNumber },
            expiryDate = expiryDate.ifBlank { currentInv.expiryDate },
            supplierName = supplierName.ifBlank { currentInv.supplierName },
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        inventoryDao.insertOrUpdateInventory(updatedInv)

        val log = InventoryLog(
            date = date,
            time = time,
            actionType = "TAMBAH_STOK",
            volumeChangeMl = addMl,
            remainingStockMl = newStock,
            batchNumber = batchNumber.ifBlank { currentInv.batchNumber },
            officerName = officerName,
            notes = notes.ifBlank { "Tambah stok baharu: +${addMl.toInt()} mL (No. Batch: ${batchNumber.ifBlank { currentInv.batchNumber }})" },
            timestamp = System.currentTimeMillis()
        )
        inventoryDao.insertInventoryLog(log)
    }

    suspend fun adjustStockLevel(
        newStockMl: Double,
        officerName: String,
        date: String,
        time: String,
        reason: String
    ) {
        val currentInv = inventoryDao.getInventory() ?: InventoryItem()
        val diffMl = newStockMl - currentInv.currentStockMl
        val updatedInv = currentInv.copy(
            currentStockMl = newStockMl,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        inventoryDao.insertOrUpdateInventory(updatedInv)

        val log = InventoryLog(
            date = date,
            time = time,
            actionType = "PELARASAN_MANUAL",
            volumeChangeMl = diffMl,
            remainingStockMl = newStockMl,
            batchNumber = currentInv.batchNumber,
            officerName = officerName,
            notes = "Pelarasan stok manual: ${if (diffMl >= 0) "+$diffMl" else "$diffMl"} mL. Sebab: $reason",
            timestamp = System.currentTimeMillis()
        )
        inventoryDao.insertInventoryLog(log)
    }

    suspend fun deleteDispenseRecord(record: DispenseRecord) {
        dispenseDao.deleteRecord(record)
    }
}

