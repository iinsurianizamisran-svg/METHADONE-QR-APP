package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DispenseRecord
import com.example.data.model.InventoryItem
import com.example.data.model.InventoryLog
import com.example.data.model.Patient
import com.example.data.repository.MethadoneRepository
import com.example.util.QrCodeUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface ScanStatus {
    data object Idle : ScanStatus
    data class ReadyToDispense(val patient: Patient, val warning: String? = null) : ScanStatus
    data class AlreadyDispensedToday(val patient: Patient, val previousRecord: DispenseRecord) : ScanStatus
    data class PatientSuspended(val patient: Patient, val reason: String) : ScanStatus
    data class NotFound(val rawCode: String) : ScanStatus
    data class DispenseSuccess(val patient: Patient, val record: DispenseRecord) : ScanStatus
}

data class AttendanceSummary(
    val totalRegistered: Int = 0,
    val attendedCount: Int = 0,
    val pendingCount: Int = 0,
    val totalMgDispensed: Double = 0.0,
    val totalMlDispensed: Double = 0.0,
    val dotCount: Int = 0,
    val takeHomeCount: Int = 0
)

class MethadoneViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MethadoneRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MethadoneRepository(db.patientDao(), db.dispenseDao(), db.inventoryDao())
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Idle)
    val scanStatus: StateFlow<ScanStatus> = _scanStatus.asStateFlow()

    private val _fastTrackMode = MutableStateFlow(false)
    val fastTrackMode: StateFlow<Boolean> = _fastTrackMode.asStateFlow()

    private val _activeOfficerName = MutableStateFlow("Jururawat Kanan (Farmasi)")
    val activeOfficerName: StateFlow<String> = _activeOfficerName.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val patients: StateFlow<List<Patient>> = _searchQuery
        .flatMapLatest { query -> repository.searchPatients(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dateRecords: StateFlow<List<DispenseRecord>> = _selectedDate
        .flatMapLatest { date -> repository.getRecordsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<DispenseRecord>> = repository.allDispenseRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val inventoryItem: StateFlow<InventoryItem> = repository.inventoryItem
        .map { item -> item ?: InventoryItem() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventoryItem())

    val inventoryLogs: StateFlow<List<InventoryLog>> = repository.inventoryLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isLowStockAlert: StateFlow<Boolean> = inventoryItem
        .map { item -> item.isLowStock }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val missedDoseAlerts: StateFlow<List<Patient>> = patients
        .map { list -> list.filter { it.status == "AKTIF" && it.missedDaysStreak > 3 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceSummary: StateFlow<AttendanceSummary> = combine(
        patients,
        dateRecords
    ) { allPatientsList, recordsToday ->
        val total = allPatientsList.size
        val attendedPatientIds = recordsToday.map { it.patientId }.toSet()
        val attended = attendedPatientIds.size
        val pending = (total - attended).coerceAtLeast(0)
        val totalMg = recordsToday.sumOf { it.doseMg }
        val totalMl = recordsToday.sumOf { it.doseVolumeMl }
        val dot = recordsToday.count { it.dispenseType == "DOT" }
        val takeHome = recordsToday.count { it.dispenseType == "TAKE_HOME" }

        AttendanceSummary(
            totalRegistered = total,
            attendedCount = attended,
            pendingCount = pending,
            totalMgDispensed = totalMg,
            totalMlDispensed = totalMl,
            dotCount = dot,
            takeHomeCount = takeHome
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AttendanceSummary())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun toggleFastTrackMode(enabled: Boolean) {
        _fastTrackMode.value = enabled
    }

    fun updateOfficerName(name: String) {
        _activeOfficerName.value = name
    }

    fun clearScanStatus() {
        _scanStatus.value = ScanStatus.Idle
    }

    fun processScannedQr(rawContent: String, method: String = "PENGIMBAS_QR") {
        viewModelScope.launch {
            val patientIdOrIc = QrCodeUtil.extractPatientIdentifier(rawContent)
            var patient = repository.getPatientById(patientIdOrIc)
            if (patient == null) {
                patient = repository.getPatientByIc(patientIdOrIc)
            }

            if (patient == null) {
                _scanStatus.value = ScanStatus.NotFound(rawContent)
                return@launch
            }

            if (patient.status == "GANTUNG" || patient.status == "TAMAT") {
                _scanStatus.value = ScanStatus.PatientSuspended(
                    patient = patient,
                    reason = "Status Pesakit: ${patient.status}. Sila rujuk Pegawai Perubatan sebelum pemberian ubat."
                )
                return@launch
            }

            val today = getTodayDateString()
            val existingRecord = repository.getRecordForPatientToday(patient.patientId, today)

            if (existingRecord != null) {
                _scanStatus.value = ScanStatus.AlreadyDispensedToday(patient, existingRecord)
            } else {
                val warning = if (patient.missedDaysStreak > 3) {
                    "AMARAN KRITIKAL: Pesakit tidak hadir selama ${patient.missedDaysStreak} HARI BERTURUT-TURUT! (Melebihi had 3 hari). Sila pastikan penilaian toleransi dos oleh Doktor/FMS dilakukan sebelum mengesahkan pemberian dos baharu."
                } else if (patient.missedDaysStreak > 0) {
                    "PERINGATAN: Pesakit tidak hadir selama ${patient.missedDaysStreak} hari berturut-turut. Sila ingatkan pesakit mengenai pematuhan dos harian."
                } else null

                _scanStatus.value = ScanStatus.ReadyToDispense(patient, warning)
            }
        }
    }

    fun confirmDispense(
        patient: Patient,
        customDoseMg: Double? = null,
        dispenseType: String = patient.dispenseType,
        bottlesCount: Int = if (dispenseType == "TAKE_HOME") (if (patient.takeHomeDays > 0) patient.takeHomeDays else 1) else 0,
        bottlesReturned: Boolean = true,
        remarks: String = "",
        scanMethod: String = "PENGIMBAS_QR"
    ) {
        viewModelScope.launch {
            val now = Date()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)

            val doseMg = customDoseMg ?: patient.currentDoseMg
            val doseMl = String.format(Locale.US, "%.1f", doseMg / 5.0).toDouble()

            val record = DispenseRecord(
                patientId = patient.patientId,
                patientName = patient.name,
                patientIc = patient.icNumber,
                dispenseDate = todayStr,
                dispenseTime = timeStr,
                doseMg = doseMg,
                doseVolumeMl = doseMl,
                dispenseType = dispenseType,
                takeHomeBottlesCount = bottlesCount,
                bottlesReturned = bottlesReturned,
                officerName = _activeOfficerName.value,
                scanMethod = scanMethod,
                attendanceStatus = "HADIR_DISPENSI",
                remarks = remarks.ifBlank {
                    if (dispenseType == "DOT") {
                        "Ubat diminum di hadapan petugas (DOT). Kehadiran direkodkan melalui $scanMethod."
                    } else {
                        "Dispensasi $bottlesCount botol Bawa Balik (Take-Home). Botol dipulangkan: ${if (bottlesReturned) "Ya" else "Tidak"}."
                    }
                },
                timestamp = System.currentTimeMillis()
            )

            repository.recordDispense(record)

            // Update patient missed days streak to 0 and last dispensed info
            val updatedPatient = patient.copy(
                missedDaysStreak = 0,
                lastDispensedDate = todayStr,
                lastDispensedTime = timeStr
            )
            repository.updatePatient(updatedPatient)

            _scanStatus.value = ScanStatus.DispenseSuccess(updatedPatient, record)
        }
    }

    fun resolveMissedDoseAlert(patient: Patient, staffNotes: String) {
        viewModelScope.launch {
            val updatedPatient = patient.copy(
                missedDaysStreak = 0,
                notes = "${patient.notes}\n[AMARAN CICIR DOS DISELESAIKAN]: $staffNotes (${_activeOfficerName.value})"
            )
            repository.updatePatient(updatedPatient)
        }
    }

    private val _lastRegisteredPatient = MutableStateFlow<Patient?>(null)
    val lastRegisteredPatient: StateFlow<Patient?> = _lastRegisteredPatient.asStateFlow()

    fun clearLastRegisteredPatient() {
        _lastRegisteredPatient.value = null
    }

    fun addNewPatient(
        name: String,
        icNumber: String,
        phone: String,
        doseMg: Double,
        dispenseType: String,
        takeHomeDays: Int,
        doctor: String,
        clinic: String,
        notes: String,
        onSuccess: ((Patient) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val randomSuffix = (100..999).random()
            val newPatientId = "METH-2026-0$randomSuffix"
            val doseMl = String.format(Locale.US, "%.1f", doseMg / 5.0).toDouble()
            val today = getTodayDateString()

            val patient = Patient(
                patientId = newPatientId,
                name = name.trim(),
                icNumber = icNumber.trim(),
                phoneNumber = phone.trim().ifBlank { "012-0000000" },
                currentDoseMg = doseMg,
                doseVolumeMl = doseMl,
                dispenseType = dispenseType,
                takeHomeDays = if (dispenseType == "TAKE_HOME") takeHomeDays else 0,
                registrationDate = today,
                clinicLocation = clinic.ifBlank { "Klinik Kesihatan Cheras" },
                doctorName = doctor.ifBlank { "Dr. Farah Hanim (FMS)" },
                status = "AKTIF",
                notes = notes.ifBlank { "Pendaftaran baharu pesakit rawatan terapi gentian metadon." }
            )

            repository.insertPatient(patient)
            _lastRegisteredPatient.value = patient
            onSuccess?.invoke(patient)
        }
    }

    fun updatePatient(patient: Patient) {
        viewModelScope.launch {
            repository.updatePatient(patient)
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            repository.deletePatient(patient)
        }
    }

    fun deleteRecord(record: DispenseRecord) {
        viewModelScope.launch {
            repository.deleteDispenseRecord(record)
        }
    }

    fun updateReorderThreshold(newThresholdMl: Double) {
        viewModelScope.launch {
            val now = Date()
            val dateStr = getTodayDateString()
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
            repository.updateReorderThreshold(
                newThresholdMl = newThresholdMl,
                officerName = _activeOfficerName.value,
                date = dateStr,
                time = timeStr
            )
        }
    }

    fun restockInventory(
        addMl: Double,
        batchNumber: String,
        expiryDate: String,
        supplierName: String,
        notes: String
    ) {
        viewModelScope.launch {
            val now = Date()
            val dateStr = getTodayDateString()
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
            repository.restockInventory(
                addMl = addMl,
                batchNumber = batchNumber,
                expiryDate = expiryDate,
                supplierName = supplierName,
                officerName = _activeOfficerName.value,
                date = dateStr,
                time = timeStr,
                notes = notes
            )
        }
    }

    fun adjustStockLevel(newStockMl: Double, reason: String) {
        viewModelScope.launch {
            val now = Date()
            val dateStr = getTodayDateString()
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
            repository.adjustStockLevel(
                newStockMl = newStockMl,
                officerName = _activeOfficerName.value,
                date = dateStr,
                time = timeStr,
                reason = reason
            )
        }
    }

    companion object {
        fun getTodayDateString(): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }
}
