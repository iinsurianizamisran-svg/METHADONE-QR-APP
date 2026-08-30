package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ClinicSettings
import com.example.ui.viewmodel.MethadoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicSetupScreen(
    viewModel: MethadoneViewModel,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val currentSettings by viewModel.clinicSettings.collectAsStateWithLifecycle()

    var clinicName by remember(currentSettings) { mutableStateOf(currentSettings?.clinicName ?: "e-Methadone PKD Kluang") }
    var activeCountText by remember(currentSettings) { mutableStateOf((currentSettings?.activePatientsCount ?: 45).toString()) }
    var newCasesText by remember(currentSettings) { mutableStateOf((currentSettings?.newCasesCount ?: 5).toString()) }
    var defaultersText by remember(currentSettings) { mutableStateOf((currentSettings?.defaultersCount ?: 2).toString()) }
    var restartText by remember(currentSettings) { mutableStateOf((currentSettings?.restartCount ?: 1).toString()) }
    var transferInText by remember(currentSettings) { mutableStateOf((currentSettings?.transferInCount ?: 3).toString()) }
    var transferOutText by remember(currentSettings) { mutableStateOf((currentSettings?.transferOutCount ?: 1).toString()) }
    var deathText by remember(currentSettings) { mutableStateOf((currentSettings?.deathCount ?: 0).toString()) }
    var terminatedText by remember(currentSettings) { mutableStateOf((currentSettings?.terminatedCount ?: 1).toString()) }
    var batchNumberText by remember(currentSettings) { mutableStateOf(currentSettings?.initialBatchNumber ?: "MTH-2026-B892") }
    var expiryDateText by remember(currentSettings) { mutableStateOf(currentSettings?.initialExpiryDate ?: "2027-12-31") }
    var totalStockLitersText by remember(currentSettings) { mutableStateOf((currentSettings?.initialStockLiters ?: 5.0).toString()) }
    var strengthText by remember(currentSettings) { mutableStateOf(currentSettings?.initialStrength ?: "5 MG / 1 ML") }
    var backupPath by remember(currentSettings) { mutableStateOf(currentSettings?.autoBackupPath ?: "/sdcard/eMethadone_Backup") }
    var ndmaRegNo by remember(currentSettings) { mutableStateOf(currentSettings?.ndmaRegNo ?: "NDMA-KPM-9281A") }
    var ndmaStatus by remember(currentSettings) { mutableStateOf(currentSettings?.ndmaStatus ?: "Berdaftar / Aktif") }

    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val isAdmin = userRole == com.example.data.model.UserRoles.ADMIN

    val active = activeCountText.toIntOrNull() ?: 0
    val newCases = newCasesText.toIntOrNull() ?: 0
    val defaulters = defaultersText.toIntOrNull() ?: 0
    val restart = restartText.toIntOrNull() ?: 0
    val transferIn = transferInText.toIntOrNull() ?: 0
    val transferOut = transferOutText.toIntOrNull() ?: 0
    val death = deathText.toIntOrNull() ?: 0
    val terminated = terminatedText.toIntOrNull() ?: 0

    val netActiveCalculated = (active + newCases + transferIn + restart) - (defaulters + transferOut + death + terminated)

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tetapan Permulaan Klinik & NDMA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Konfigurasi NDMA (National Drugs Malaysia Association) & Stok",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    Icon(
                        Icons.Default.MedicalServices,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Konfigurasi Klinik Methadone",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Sila lengkapkan maklumat klinik, stok simpanan permulaan, dan baseline pesakit untuk Laporan NDMA (National Drugs Malaysia Association).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Error Message Banner
            AnimatedVisibility(visible = errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // 1. Clinic Information Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1. Maklumat Klinik / PKD", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Divider()

                    OutlinedTextField(
                        value = clinicName,
                        onValueChange = { clinicName = it },
                        label = { Text("Nama Klinik Methadone") },
                        placeholder = { Text("Contoh: e-Methadone PKD Kluang / KK Cheras") },
                        leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_clinic_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Pendaftaran NDMA (National Drugs Malaysia Association)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ndmaRegNo,
                            onValueChange = { ndmaRegNo = it },
                            label = { Text("No. Pendaftaran NDMA") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("setup_ndma_reg_no"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = ndmaStatus,
                            onValueChange = { ndmaStatus = it },
                            label = { Text("Status NDMA") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("setup_ndma_status"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // 2. NDMA Baseline Figures Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2. Metrik Basal Laporan NDMA Bulanan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Text(
                        text = "Masukkan angka terkini pesakit mengikut rekod daftar klinik bagi penjanaan Laporan Bulanan NDMA (National Drugs Malaysia Association) (PDF/CSV):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Divider()

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = activeCountText,
                            onValueChange = { activeCountText = it },
                            label = { Text("Active Patients") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("setup_active_count"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newCasesText,
                            onValueChange = { newCasesText = it },
                            label = { Text("New Register") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("setup_new_cases"),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = defaultersText,
                            onValueChange = { defaultersText = it },
                            label = { Text("Defaulter (Cicir)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("setup_defaulters"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = restartText,
                            onValueChange = { restartText = it },
                            label = { Text("Restart (Sambung)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("setup_restart"),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = transferInText,
                            onValueChange = { transferInText = it },
                            label = { Text("Transfer-In") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("setup_transfer_in"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = transferOutText,
                            onValueChange = { transferOutText = it },
                            label = { Text("Transfer-Out") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("setup_transfer_out"),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = deathText,
                            onValueChange = { deathText = it },
                            label = { Text("Death (Kematian)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("setup_death"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = terminatedText,
                            onValueChange = { terminatedText = it },
                            label = { Text("Terminated (Tamat)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("setup_terminated"),
                            singleLine = true
                        )
                    }

                    // Calculated Net Active Preview
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Anggaran Pesakit Aktif Akhir Bulan:", style = MaterialTheme.typography.labelMedium)
                                Text("(Active + New + TransferIn + Restart) - (Defaulter + TransferOut + Death + Terminated)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            Text(
                                text = "$netActiveCalculated Orang",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 3. Initial Methadone Stock Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "3. Rekod Permulaan Stok Methadone Didalam Simpanan",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Divider()

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = batchNumberText,
                            onValueChange = { batchNumberText = it },
                            label = { Text("Batch Ubatan") },
                            placeholder = { Text("MTH-2026-B892") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = expiryDateText,
                            onValueChange = { expiryDateText = it },
                            label = { Text("Expired Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = totalStockLitersText,
                            onValueChange = { totalStockLitersText = it },
                            label = { Text("Jumlah Stok (Liter)") },
                            placeholder = { Text("5.0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = strengthText,
                            onValueChange = { strengthText = it },
                            label = { Text("Kekuatan Ubatan") },
                            placeholder = { Text("5 MG / 1 ML") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Text(
                        text = "Maklumat ini akan dijadikan rekod baseline simpanan inventori klinik.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // 4. Auto Backup Folder Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("4. Lokasi Storage Auto Backup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Divider()

                    OutlinedTextField(
                        value = backupPath,
                        onValueChange = { backupPath = it },
                        label = { Text("Lokasi Folder Backup Selamat") },
                        placeholder = { Text("/sdcard/eMethadone_Backup") },
                        leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_backup_path_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "Setiap bulan & sesi dispensasi, pangkalan data akan dilesapkan secara automatik ke folder ini untuk perlindungan rekod pesakit.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save & Proceed Button
            Button(
                onClick = {
                    if (!isAdmin) {
                        Toast.makeText(context, "Akses Terhad: Hanya Pentadbir (Admin) yang dibenarkan menukar tetapan!", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    val stockLiters = totalStockLitersText.toDoubleOrNull() ?: 5.0
                    viewModel.saveClinicSetup(
                        clinicName = clinicName,
                        activePatients = active,
                        newCases = newCases,
                        defaulters = defaulters,
                        restart = restart,
                        transferIn = transferIn,
                        transferOut = transferOut,
                        death = death,
                        terminated = terminated,
                        initialBatchNumber = batchNumberText,
                        initialExpiryDate = expiryDateText,
                        initialStockLiters = stockLiters,
                        initialStrength = strengthText,
                        ndmaRegNo = ndmaRegNo,
                        ndmaStatus = ndmaStatus,
                        autoBackupPath = backupPath,
                        onComplete = { success, msg ->
                            if (success) {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                onSetupComplete()
                            } else {
                                errorMessage = msg
                            }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_clinic_setup_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Tetapan & Mula Guna Aplikasi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
