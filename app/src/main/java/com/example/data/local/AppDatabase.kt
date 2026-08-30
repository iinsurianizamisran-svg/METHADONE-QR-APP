package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ClinicSettings
import com.example.data.model.DispenseRecord
import com.example.data.model.InventoryItem
import com.example.data.model.InventoryLog
import com.example.data.model.Patient
import com.example.data.model.User
import com.example.data.model.UserRoles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [Patient::class, DispenseRecord::class, InventoryItem::class, InventoryLog::class, User::class, ClinicSettings::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun dispenseDao(): DispenseDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun userDao(): UserDao
    abstract fun clinicSettingsDao(): ClinicSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "methadone_clinic_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(
                            database.patientDao(),
                            database.dispenseDao(),
                            database.inventoryDao(),
                            database.userDao(),
                            database.clinicSettingsDao()
                        )
                    }
                }
            }
        }

        private suspend fun populateInitialData(
            patientDao: PatientDao,
            dispenseDao: DispenseDao,
            inventoryDao: InventoryDao,
            userDao: UserDao,
            clinicSettingsDao: ClinicSettingsDao
        ) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Seed initial Clinic Settings
            clinicSettingsDao.saveClinicSettings(
                ClinicSettings(
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

            // Seed default staff user accounts
            val defaultUsers = listOf(
                User(
                    username = "admin",
                    passwordHash = "admin",
                    fullName = "Pentadbir Utama Sistem",
                    role = UserRoles.ADMIN,
                    icOrStaffId = "ADMIN-001",
                    email = "admin@kkcheras.gov.my",
                    createdDate = today
                ),
                User(
                    username = "farmasi",
                    passwordHash = "farmasi123",
                    fullName = "Petugas Farmasi Azman bin Daud",
                    role = UserRoles.PHARMACY,
                    icOrStaffId = "FAR-88102",
                    email = "azman.pharmacy@kkcheras.gov.my",
                    createdDate = today
                ),
                User(
                    username = "doktor",
                    passwordHash = "doktor123",
                    fullName = "Dr. Farah Hanim (FMS)",
                    role = UserRoles.DOCTOR,
                    icOrStaffId = "DOC-77201",
                    email = "drfarah@kkcheras.gov.my",
                    createdDate = today
                ),
                User(
                    username = "amo",
                    passwordHash = "amo123",
                    fullName = "PPP Hafiz (AMO Kanan)",
                    role = UserRoles.AMO,
                    icOrStaffId = "AMO-90112",
                    email = "hafiz.amo@kkcheras.gov.my",
                    createdDate = today
                )
            )

            userDao.insertUsers(defaultUsers)

            val samplePatients = listOf(
                Patient(
                    patientId = "METH-2026-0012",
                    name = "Ahmad Razali bin Mohd Kassim",
                    icNumber = "840512-10-5543",
                    phoneNumber = "012-3894521",
                    currentDoseMg = 75.0,
                    doseVolumeMl = 15.0,
                    dispenseType = "DOT",
                    takeHomeDays = 0,
                    registrationDate = "2024-03-10",
                    clinicLocation = "Klinik Kesihatan Cheras",
                    doctorName = "Dr. Farah Hanim (FMS)",
                    status = "AKTIF",
                    notes = "Pesakit patuh harian. Ujian saringan berkala memuaskan.",
                    lastDispensedDate = today,
                    lastDispensedTime = "08:15:30",
                    missedDaysStreak = 0,
                    lastXRayDate = "2026-01-15",
                    lastXRayResult = "Normal",
                    lastBloodTestDate = "2026-02-10",
                    hivResult = "Non-Reactive",
                    hepBResult = "Non-Reactive",
                    hepCResult = "Non-Reactive",
                    lftResult = "Normal"
                ),
                Patient(
                    patientId = "METH-2026-0034",
                    name = "Tan Wei Lun",
                    icNumber = "791104-14-6127",
                    phoneNumber = "017-6629183",
                    currentDoseMg = 90.0,
                    doseVolumeMl = 18.0,
                    dispenseType = "TAKE_HOME",
                    takeHomeDays = 3,
                    registrationDate = "2023-11-20",
                    clinicLocation = "Klinik Kesihatan Cheras",
                    doctorName = "Dr. Zulkifli bin Ismail",
                    status = "AKTIF",
                    notes = "Layak Bawa Balik 3 hari (Pekerjaan syif malam). Botol kosong perlu dipulangkan.",
                    lastDispensedDate = today,
                    lastDispensedTime = "08:42:10",
                    missedDaysStreak = 0,
                    lastXRayDate = "2023-11-25",
                    lastXRayResult = "Normal",
                    lastBloodTestDate = "2025-03-05", // Overdue > 365 days
                    hivResult = "Non-Reactive",
                    hepBResult = "Non-Reactive",
                    hepCResult = "Non-Reactive",
                    lftResult = "Normal"
                ),
                Patient(
                    patientId = "METH-2026-0056",
                    name = "M. Sureshkumar a/l Muniandy",
                    icNumber = "880228-08-5931",
                    phoneNumber = "019-4820194",
                    currentDoseMg = 60.0,
                    doseVolumeMl = 12.0,
                    dispenseType = "DOT",
                    takeHomeDays = 0,
                    registrationDate = "2024-08-01",
                    clinicLocation = "Klinik Kesihatan Cheras",
                    doctorName = "Dr. Farah Hanim (FMS)",
                    status = "AKTIF",
                    notes = "Program penstabilan dos. Temujanji kaunseling seterusnya 15hb.",
                    lastDispensedDate = null,
                    lastDispensedTime = null,
                    missedDaysStreak = 0
                ),
                Patient(
                    patientId = "METH-2026-0078",
                    name = "Nur Hidayah binti Osman",
                    icNumber = "910715-01-5820",
                    phoneNumber = "013-9012384",
                    currentDoseMg = 50.0,
                    doseVolumeMl = 10.0,
                    dispenseType = "DOT",
                    takeHomeDays = 0,
                    registrationDate = "2025-01-12",
                    clinicLocation = "Klinik Kesihatan Cheras",
                    doctorName = "Dr. Farah Hanim (FMS)",
                    status = "AKTIF",
                    notes = "Kesihatan stabil. Rawatan integrasi Klinik Kesihatan Ibu & Anak.",
                    lastDispensedDate = null,
                    lastDispensedTime = null,
                    missedDaysStreak = 0,
                    lastXRayDate = null, // Missing Chest X-Ray (Saringan Wajib Pesakit Baru)
                    lastXRayResult = null,
                    lastBloodTestDate = "2026-01-20",
                    hivResult = "Non-Reactive",
                    hepBResult = "Non-Reactive",
                    hepCResult = "Non-Reactive",
                    lftResult = "Normal"
                ),
                Patient(
                    patientId = "METH-2026-0091",
                    name = "Mohd Faizal bin Kamaruddin",
                    icNumber = "850903-10-6729",
                    phoneNumber = "018-7712390",
                    currentDoseMg = 80.0,
                    doseVolumeMl = 16.0,
                    dispenseType = "DOT",
                    takeHomeDays = 0,
                    registrationDate = "2024-05-18",
                    clinicLocation = "Klinik Kesihatan Cheras",
                    doctorName = "Dr. Zulkifli bin Ismail",
                    status = "AKTIF",
                    notes = "Perhatian: Peringatan temujanji ujian fungsi hati.",
                    lastDispensedDate = null,
                    lastDispensedTime = null,
                    missedDaysStreak = 1,
                    lastXRayDate = "2024-05-20",
                    lastXRayResult = "Normal",
                    lastBloodTestDate = "2026-05-02",
                    hivResult = "Non-Reactive",
                    hepBResult = "Non-Reactive",
                    hepCResult = "Non-Reactive",
                    lftResult = "Normal"
                ),
                Patient(
                    patientId = "METH-2026-0105",
                    name = "Chong Chee Keong",
                    icNumber = "730319-14-5311",
                    phoneNumber = "016-5541092",
                    currentDoseMg = 100.0,
                    doseVolumeMl = 20.0,
                    dispenseType = "TAKE_HOME",
                    takeHomeDays = 7,
                    registrationDate = "2022-06-05",
                    clinicLocation = "Klinik Kesihatan Cheras",
                    doctorName = "Dr. Farah Hanim (FMS)",
                    status = "AKTIF",
                    notes = "Pesakit kategori cemerlang > 3 tahun. Pengambilan ubat mingguan (7 hari).",
                    lastDispensedDate = null,
                    lastDispensedTime = null,
                    missedDaysStreak = 0,
                    lastXRayDate = "2022-06-10",
                    lastXRayResult = "Normal",
                    lastBloodTestDate = "2026-01-20",
                    hivResult = "Non-Reactive",
                    hepBResult = "Non-Reactive",
                    hepCResult = "Non-Reactive",
                    lftResult = "Normal",
                    lastEcgDate = "2025-01-10", // Overdue > 365 days for dose >= 100mg
                    lastEcgResult = "Normal",
                    lastEcgQtcMs = 430
                ),
                Patient(
                    patientId = "METH-2026-0118",
                    name = "Khairul Anuar bin Hassan",
                    icNumber = "810405-03-5119",
                    phoneNumber = "011-23456789",
                    currentDoseMg = 70.0,
                    doseVolumeMl = 14.0,
                    dispenseType = "DOT",
                    takeHomeDays = 0,
                    registrationDate = "2024-02-14",
                    clinicLocation = "Klinik Kesihatan Cheras",
                    doctorName = "Dr. Farah Hanim (FMS)",
                    status = "AKTIF",
                    notes = "AMARAN: Pesakit cicir dos 4 hari berturut-turut. Rujuk Pegawai Perubatan sebelum pemberian ubat.",
                    lastDispensedDate = "2026-08-25",
                    lastDispensedTime = "09:10:00",
                    missedDaysStreak = 4
                ),
                Patient(
                    patientId = "METH-2026-0142",
                    name = "Ramasamy a/l Subramaniam",
                    icNumber = "770819-02-5401",
                    phoneNumber = "014-8899123",
                    currentDoseMg = 85.0,
                    doseVolumeMl = 17.0,
                    dispenseType = "DOT",
                    takeHomeDays = 0,
                    registrationDate = "2023-09-01",
                    clinicLocation = "Klinik Kesihatan Cheras",
                    doctorName = "Dr. Zulkifli bin Ismail",
                    status = "AKTIF",
                    notes = "AMARAN: Cicir dos 5 hari berturut-turut. Toleransi dos perlu dinilai semula.",
                    lastDispensedDate = "2026-08-24",
                    lastDispensedTime = "10:30:00",
                    missedDaysStreak = 5
                )
            )

            patientDao.insertPatients(samplePatients)

            // Seed 2 dispense records for today so attendance displays active stats immediately
            val initialDispenses = listOf(
                DispenseRecord(
                    patientId = "METH-2026-0012",
                    patientName = "Ahmad Razali bin Mohd Kassim",
                    patientIc = "840512-10-5543",
                    dispenseDate = today,
                    dispenseTime = "08:15:30",
                    doseMg = 75.0,
                    doseVolumeMl = 15.0,
                    dispenseType = "DOT",
                    takeHomeBottlesCount = 0,
                    bottlesReturned = true,
                    officerName = "Jururawat Kanan Siti",
                    scanMethod = "PENGIMBAS_QR",
                    attendanceStatus = "HADIR_DISPENSI",
                    remarks = "Ubat diminum sepenuhnya di hadapan petugas (DOT). Kod QR disahkan.",
                    timestamp = System.currentTimeMillis() - 3600000
                ),
                DispenseRecord(
                    patientId = "METH-2026-0034",
                    patientName = "Tan Wei Lun",
                    patientIc = "791104-14-6127",
                    dispenseDate = today,
                    dispenseTime = "08:42:10",
                    doseMg = 90.0,
                    doseVolumeMl = 18.0,
                    dispenseType = "TAKE_HOME",
                    takeHomeBottlesCount = 3,
                    bottlesReturned = true,
                    officerName = "Petugas Farmasi Azman",
                    scanMethod = "PENGIMBAS_QR",
                    attendanceStatus = "HADIR_DISPENSI",
                    remarks = "3 botol Bawa Balik diserahkan. 3 botol kosong telah dipulangkan dan diperiksa.",
                    timestamp = System.currentTimeMillis() - 1800000
                )
            )

            dispenseDao.insertRecords(initialDispenses)

            // Seed initial inventory item and inventory logs
            val initialInventory = InventoryItem(
                id = 1,
                medicationName = "Methadone Oral Concentrate 5mg/mL",
                currentStockMl = 4500.0,
                reorderThresholdMl = 1000.0,
                unitConcentrationMgPerMl = 5.0,
                lastRestockDate = today,
                lastRestockMl = 5000.0,
                batchNumber = "MTH-2026-B892",
                expiryDate = "2027-08-31",
                supplierName = "PharmaCare Malaysia / KKM Store",
                lastUpdatedTimestamp = System.currentTimeMillis()
            )

            inventoryDao.insertOrUpdateInventory(initialInventory)

            val initialLogs = listOf(
                InventoryLog(
                    date = today,
                    time = "07:30:00",
                    actionType = "TAMBAH_STOK",
                    volumeChangeMl = 5000.0,
                    remainingStockMl = 5000.0,
                    batchNumber = "MTH-2026-B892",
                    officerName = "Pegawai Farmasi Azman bin Daud",
                    notes = "Penerimaan bekalan bulanan metadon syrup 5mg/mL daripada Logistik Farmasi KKM.",
                    timestamp = System.currentTimeMillis() - 7200000
                ),
                InventoryLog(
                    date = today,
                    time = "08:15:30",
                    actionType = "DISPENSI_AUTOMATIK",
                    volumeChangeMl = -15.0,
                    remainingStockMl = 4985.0,
                    batchNumber = "MTH-2026-B892",
                    officerName = "Jururawat Kanan Siti",
                    notes = "Dispensasi Ahmad Razali bin Mohd Kassim (METH-2026-0012): 15.0 mL",
                    timestamp = System.currentTimeMillis() - 3600000
                ),
                InventoryLog(
                    date = today,
                    time = "08:42:10",
                    actionType = "DISPENSI_AUTOMATIK",
                    volumeChangeMl = -72.0,
                    remainingStockMl = 4913.0,
                    batchNumber = "MTH-2026-B892",
                    officerName = "Petugas Farmasi Azman",
                    notes = "Dispensasi Tan Wei Lun (METH-2026-0034): 72.0 mL (4 botol x 18.0 mL)",
                    timestamp = System.currentTimeMillis() - 1800000
                )
            )

            initialLogs.forEach { inventoryDao.insertInventoryLog(it) }
        }
    }
}
