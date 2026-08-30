package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ClinicSettings
import com.example.data.model.DispenseRecord
import com.example.data.model.InventoryItem
import com.example.data.model.InventoryLog
import com.example.data.model.Patient
import com.example.data.model.User
import com.example.data.model.UserRoles
import com.example.data.repository.MethadoneRepository
import com.example.util.QrCodeUtil
import java.io.File
import java.io.FileOutputStream
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
        repository = MethadoneRepository(
            db.patientDao(),
            db.dispenseDao(),
            db.inventoryDao(),
            db.userDao(),
            db.clinicSettingsDao()
        )

        // Automatic Status Update for Defaulter / Cicir Rawatan > 4 days consecutive
        viewModelScope.launch {
            patients.collect { list ->
                list.filter { it.status == "AKTIF" && it.missedDaysStreak >= 4 }.forEach { p ->
                    val updated = p.copy(
                        status = "CICIR",
                        notes = "${p.notes}\n[AUTOMATIK]: Status dikemaskini ke CICIR (Defaulter) kerana tidak hadir ${p.missedDaysStreak} hari berturut-turut (> 4 hari)."
                    )
                    repository.updatePatient(updated)
                }
            }
        }
    }

    val clinicSettings: StateFlow<ClinicSettings?> = repository.clinicSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ClinicSettings(
                id = 1,
                clinicName = "e-Methadone PKD Kluang",
                isSetupCompleted = false,
                activePatientsCount = 45,
                newCasesCount = 5,
                defaultersCount = 2,
                restartCount = 1,
                transferInCount = 3,
                transferOutCount = 1,
                deathCount = 0,
                terminatedCount = 1,
                autoBackupPath = "/sdcard/eMethadone_Backup"
            )
        )

    fun saveClinicSetup(
        clinicName: String,
        activePatients: Int,
        newCases: Int,
        defaulters: Int,
        restart: Int,
        transferIn: Int,
        transferOut: Int,
        death: Int,
        terminated: Int,
        initialBatchNumber: String = "MTH-2026-B892",
        initialExpiryDate: String = "2027-12-31",
        initialStockLiters: Double = 5.0,
        initialStrength: String = "5 mg / 1 ml",
        autoBackupPath: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val name = clinicName.trim()
            if (name.isEmpty()) {
                onComplete(false, "Sila masukkan Nama Klinik Methadone.")
                return@launch
            }

            val current = clinicSettings.value ?: ClinicSettings()
            val updated = current.copy(
                clinicName = name,
                isSetupCompleted = true,
                activePatientsCount = activePatients,
                newCasesCount = newCases,
                defaultersCount = defaulters,
                restartCount = restart,
                transferInCount = transferIn,
                transferOutCount = transferOut,
                deathCount = death,
                terminatedCount = terminated,
                initialBatchNumber = initialBatchNumber,
                initialExpiryDate = initialExpiryDate,
                initialStockLiters = initialStockLiters,
                initialStrength = initialStrength,
                autoBackupPath = autoBackupPath.trim().ifEmpty { "/sdcard/eMethadone_Backup" }
            )

            repository.saveClinicSettings(updated)
            onComplete(true, "Konfigurasi Klinik '$name' & Stok Permulaan ($initialStockLiters L) berjaya disimpan!")
        }
    }

    fun triggerAutoBackup(context: android.content.Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentConfig = clinicSettings.value ?: ClinicSettings()
                val targetFolderPath = currentConfig.autoBackupPath.trim()
                
                // Create backup directory in internal app storage or configured path
                val backupDir = File(context.filesDir, "backups").apply { mkdirs() }
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val backupFile = File(backupDir, "eMethadone_Backup_${timestamp}.csv")

                val allPatientList = patients.value
                val records = allRecords.value

                val sb = StringBuilder()
                sb.append("\uFEFF")
                sb.appendLine("# BACKUP DATA AUTOMATIK e-METHADONE MMT")
                sb.appendLine("# Klinik: ${currentConfig.clinicName}")
                sb.appendLine("# Tarikh Backup: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
                sb.appendLine("# Total Pesakit: ${allPatientList.size}")
                sb.appendLine("# Total Rekod Dispensasi: ${records.size}")
                sb.appendLine("#")

                sb.appendLine("PATIENTS_HEADER: ID,Nama,NoKP,Dos,Jenis,Status,Telefon")
                allPatientList.forEach { p ->
                    sb.appendLine("PATIENT:${p.patientId},\"${p.name}\",${p.icNumber},${p.currentDoseMg},${p.dispenseType},${p.status},${p.phoneNumber}")
                }

                sb.appendLine("DISPENSE_HEADER: ID,Tarikh,Masa,IDPesakit,Nama,DosMg,DosMl,Petugas")
                records.forEach { r ->
                    sb.appendLine("DISPENSE:${r.recordId},${r.dispenseDate},${r.dispenseTime},${r.patientId},\"${r.patientName}\",${r.doseMg},${r.doseVolumeMl},\"${r.officerName}\"")
                }

                FileOutputStream(backupFile).use { fos ->
                    fos.write(sb.toString().toByteArray(Charsets.UTF_8))
                }

                val updatedSettings = currentConfig.copy(
                    lastBackupDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )
                repository.saveClinicSettings(updatedSettings)

                onResult(true, "Auto Backup Berjaya!\nLokasi: ${backupFile.absolutePath}")
            } catch (e: Exception) {
                onResult(false, "Ralat Auto Backup: ${e.message}")
            }
        }
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userRole = MutableStateFlow(UserRoles.ADMIN)
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Idle)
    val scanStatus: StateFlow<ScanStatus> = _scanStatus.asStateFlow()

    private val _fastTrackMode = MutableStateFlow(false)
    val fastTrackMode: StateFlow<Boolean> = _fastTrackMode.asStateFlow()

    private val _activeOfficerName = MutableStateFlow("Pentadbir Sistem (admin)")
    val activeOfficerName: StateFlow<String> = _activeOfficerName.asStateFlow()

    fun login(
        usernameInput: String,
        passwordInput: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedUsername = usernameInput.trim()
            val trimmedPassword = passwordInput.trim()

            if (trimmedUsername.isEmpty() || trimmedPassword.isEmpty()) {
                onResult(false, "Sila masukkan nama pengguna dan kata laluan.")
                return@launch
            }

            // Query database for user
            val existingUser = repository.getUserByUsername(trimmedUsername)

            if (existingUser != null && existingUser.passwordHash == trimmedPassword) {
                _currentUser.value = existingUser
                _userRole.value = existingUser.role
                _activeOfficerName.value = "${existingUser.fullName} (${existingUser.role})"
                _isLoggedIn.value = true
                onResult(true, "Selamat Datang, ${existingUser.fullName}!")
            } else if (trimmedUsername.equals("admin", ignoreCase = true) && trimmedPassword == "admin") {
                // Hardcoded fallback safety for admin/admin
                val adminUser = User(
                    username = "admin",
                    passwordHash = "admin",
                    fullName = "Pentadbir Utama Sistem",
                    role = UserRoles.ADMIN,
                    icOrStaffId = "ADMIN-001",
                    createdDate = getTodayDateString()
                )
                _currentUser.value = adminUser
                _userRole.value = UserRoles.ADMIN
                _activeOfficerName.value = "Pentadbir Utama Sistem (Admin)"
                _isLoggedIn.value = true
                onResult(true, "Log masuk Pentadbir Utama berjaya!")
            } else {
                onResult(false, "Log masuk gagal! Nama pengguna atau kata laluan tidak sah.")
            }
        }
    }

    fun registerUser(
        fullName: String,
        icOrStaffId: String,
        username: String,
        password: String,
        role: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val name = fullName.trim()
            val id = icOrStaffId.trim()
            val uname = username.trim()
            val pass = password.trim()

            if (name.length < 3) {
                onResult(false, "Sila masukkan nama penuh yang sah.")
                return@launch
            }
            if (id.isEmpty()) {
                onResult(false, "Sila masukkan No. K/P atau ID Staf.")
                return@launch
            }
            if (uname.length < 3) {
                onResult(false, "Nama pengguna mestilah sekurang-kurangnya 3 aksara.")
                return@launch
            }
            if (pass.length < 4) {
                onResult(false, "Kata laluan mestilah sekurang-kurangnya 4 aksara.")
                return@launch
            }

            val checkExistingUname = repository.getUserByUsername(uname)
            if (checkExistingUname != null) {
                onResult(false, "Nama pengguna '$uname' telah didaftarkan. Sila guna nama pengguna lain.")
                return@launch
            }

            val checkExistingId = repository.getUserByIcOrStaffId(id)
            if (checkExistingId != null) {
                onResult(false, "No. K/P / ID Staf '$id' telah mempunyai akaun berdaftar.")
                return@launch
            }

            val newUser = User(
                username = uname,
                passwordHash = pass,
                fullName = name,
                role = role,
                icOrStaffId = id,
                createdDate = getTodayDateString()
            )

            repository.insertUser(newUser)
            onResult(true, "Pendaftaran akaun $role berjaya! Anda boleh log masuk sekarang.")
        }
    }

    fun recoverOrResetPassword(
        icOrStaffId: String,
        usernameQuery: String,
        newPasswordInput: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val id = icOrStaffId.trim()
            val uname = usernameQuery.trim()
            val newPass = newPasswordInput.trim()

            if (id.isEmpty() && uname.isEmpty()) {
                onResult(false, "Sila masukkan No. K/P / ID Staf atau Nama Pengguna.")
                return@launch
            }

            val userFound = repository.findUserForRecovery(uname, id)

            if (userFound == null) {
                if (uname.equals("admin", ignoreCase = true) || id.equals("ADMIN-001", ignoreCase = true)) {
                    onResult(true, "Akaun Admin Ditemui!\nNama Pengguna: admin\nKata Laluan Default: admin")
                } else {
                    onResult(false, "Tiada akaun staf ditemui untuk padanan rekod tersebut.")
                }
                return@launch
            }

            if (newPass.isNotEmpty()) {
                if (newPass.length < 4) {
                    onResult(false, "Kata laluan baharu mestilah sekurang-kurangnya 4 aksara.")
                    return@launch
                }
                repository.updatePassword(userFound.username, newPass)
                onResult(true, "Kata laluan bagi akaun '${userFound.username}' (${userFound.role}) telah berjaya dikemaskini!")
            } else {
                onResult(true, "Akaun Staf Ditemui!\nNama Pengguna: ${userFound.username}\nNama: ${userFound.fullName}\nPeranan: ${userFound.role}")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
    }

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

    fun deleteInventoryLog(logId: Long) {
        viewModelScope.launch {
            repository.deleteInventoryLog(logId)
        }
    }

    companion object {
        fun getTodayDateString(): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }
}
