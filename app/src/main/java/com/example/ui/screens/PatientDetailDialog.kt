package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenContainer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DispenseRecord
import com.example.data.model.Patient
import com.example.ui.viewmodel.MethadoneViewModel
import com.example.util.QrCodeUtil
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailDialog(
    patient: Patient,
    viewModel: MethadoneViewModel,
    onDismiss: () -> Unit,
    onDirectDispense: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Kad QR & Profil, 1: Sejarah Pengambilan, 2: Kemas Kini Dos

    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val patientRecords = remember(allRecords, patient.patientId) {
        allRecords.filter { it.patientId == patient.patientId }
    }

    val qrBitmap = remember(patient) {
        QrCodeUtil.generateQrBitmap(patient.toQrPayload(), 600)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Profil Pesakit & Kad QR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Terapi Gantian Metadon (MMT)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Kad Digital QR") },
                    icon = { Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Sejarah (${patientRecords.size})") },
                    icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Kemas Kini") },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Saringan & ECG") },
                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // Digital QR ID Card tab
                    DigitalIdCardView(
                        patient = patient,
                        qrBitmap = qrBitmap,
                        onDirectDispense = {
                            onDismiss()
                            onDirectDispense()
                        }
                    )
                }

                1 -> {
                    // History Log tab
                    PatientHistoryView(
                        records = patientRecords,
                        onDeleteRecord = { record ->
                            viewModel.deleteRecord(record)
                        }
                    )
                }

                2 -> {
                    // Update Patient & Status tab
                    EditDoseView(
                        patient = patient,
                        onSave = { updatedPatient ->
                            viewModel.updatePatient(updatedPatient)
                            onDismiss()
                        },
                        onDeletePatient = {
                            viewModel.deletePatient(patient)
                            onDismiss()
                        }
                    )
                }

                3 -> {
                    PatientScreeningView(
                        patient = patient,
                        onSave = { updatedPatient ->
                            viewModel.updatePatient(updatedPatient)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DigitalIdCardView(
    patient: Patient,
    qrBitmap: Bitmap?,
    onDirectDispense: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High polish MOH-style ID Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Clinic Emblem Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "KEMENTERIAN KESIHATAN MALAYSIA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = patient.clinicLocation,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusGreenContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = patient.status,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Scannable QR Code
                if (qrBitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Kod QR Pesakit",
                            modifier = Modifier.size(170.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Patient Name & NRIC
                Text(
                    text = patient.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "No. K/P: ${patient.icNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "No. Pendaftaran: ${patient.patientId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Dosage & Doctor details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Dos Semasa:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = "${patient.currentDoseMg.toInt()} mg (${patient.doseVolumeMl} mL)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Kaedah:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = if (patient.dispenseType == "TAKE_HOME") "Bawa Balik (${patient.takeHomeDays} Hari)" else "DOT (Klinik)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Button
        Button(
            onClick = onDirectDispense,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("dispense_from_card_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Terus Ke Skrin Dispensasi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PatientHistoryView(
    records: List<DispenseRecord>,
    onDeleteRecord: (DispenseRecord) -> Unit = {}
) {
    if (records.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Tiada rekod dispensasi sebelum ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records) { record ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${record.dispenseDate} • ${record.dispenseTime}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${record.doseMg.toInt()} mg (${record.doseVolumeMl} mL)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onDeleteRecord(record) },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus Rekod",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kaedah: ${if (record.dispenseType == "DOT") "DOT" else "Bawa Balik (${record.takeHomeBottlesCount} botol)"} • Petugas: ${record.officerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Catatan: ${record.remarks}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditDoseView(
    patient: Patient,
    onSave: (Patient) -> Unit,
    onDeletePatient: () -> Unit = {}
) {
    var name by remember { mutableStateOf(patient.name) }
    var phone by remember { mutableStateOf(patient.phoneNumber) }
    var status by remember { mutableStateOf(patient.status) }
    var doseMgText by remember { mutableStateOf(patient.currentDoseMg.toInt().toString()) }
    var dispenseType by remember { mutableStateOf(patient.dispenseType) }
    var takeHomeDaysText by remember { mutableStateOf(patient.takeHomeDays.toString()) }
    var doctorName by remember { mutableStateOf(patient.doctorName) }
    var notes by remember { mutableStateOf(patient.notes) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Pesakit") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("No. Telefon / Hubungan Kecemasan") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Status Rawatan Pesakit:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        val statusList = listOf("AKTIF", "CICIR", "GANTUNG", "TAMAT")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            statusList.forEach { s ->
                val isSel = status == s
                OutlinedButton(
                    onClick = { status = s },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSel) {
                            when (s) {
                                "AKTIF" -> StatusGreenContainer
                                "CICIR" -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        } else Color.Transparent
                    )
                ) {
                    Text(
                        text = s,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        OutlinedTextField(
            value = doseMgText,
            onValueChange = { doseMgText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Dos Preskripsi Baharu (mg)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { dispenseType = "DOT" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (dispenseType == "DOT") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) {
                Text("DOT (Harian)")
            }

            OutlinedButton(
                onClick = { dispenseType = "TAKE_HOME" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (dispenseType == "TAKE_HOME") MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent
                )
            ) {
                Text("Bawa Balik")
            }
        }

        if (dispenseType == "TAKE_HOME") {
            OutlinedTextField(
                value = takeHomeDaysText,
                onValueChange = { takeHomeDaysText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Bilangan Hari Bawa Balik (1 - 7 hari)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = doctorName,
            onValueChange = { doctorName = it },
            label = { Text("Doktor Yang Meluluskan Perubahan Dos") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Sebab Penyelarasan Dos / Catatan Rawatan") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hapus Pesakit")
            }

            Button(
                onClick = {
                    val newDose = doseMgText.toDoubleOrNull() ?: patient.currentDoseMg
                    val newVolume = String.format(Locale.US, "%.1f", newDose / 5.0).toDouble()
                    val newDays = takeHomeDaysText.toIntOrNull() ?: 0

                    val updated = patient.copy(
                        name = name.ifBlank { patient.name },
                        phoneNumber = phone,
                        status = status,
                        currentDoseMg = newDose,
                        doseVolumeMl = newVolume,
                        dispenseType = dispenseType,
                        takeHomeDays = if (dispenseType == "TAKE_HOME") newDays else 0,
                        doctorName = doctorName.ifBlank { patient.doctorName },
                        notes = notes.ifBlank { patient.notes }
                    )
                    onSave(updated)
                },
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Rekod")
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Sahkan Hapus Pesakit", fontWeight = FontWeight.Bold) },
            text = { Text("Adakah anda pasti mahu menghapuskan rekod pesakit '${patient.name}' (ID: ${patient.patientId}) daripada pangkalan data?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeletePatient()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Padamkan Rekod")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun PatientScreeningView(
    patient: Patient,
    onSave: (Patient) -> Unit
) {
    var lastXRayDate by remember { mutableStateOf(patient.lastXRayDate ?: "") }
    var lastXRayResult by remember { mutableStateOf(patient.lastXRayResult ?: "Normal") }
    
    var lastEcgDate by remember { mutableStateOf(patient.lastEcgDate ?: "") }
    var lastEcgResult by remember { mutableStateOf(patient.lastEcgResult ?: "Normal") }
    var lastEcgQtcMsText by remember { mutableStateOf(patient.lastEcgQtcMs?.toString() ?: "") }
    
    var lastBloodTestDate by remember { mutableStateOf(patient.lastBloodTestDate ?: "") }
    var hivResult by remember { mutableStateOf(patient.hivResult ?: "Non-Reactive") }
    var hepBResult by remember { mutableStateOf(patient.hepBResult ?: "Non-Reactive") }
    var hepCResult by remember { mutableStateOf(patient.hepCResult ?: "Non-Reactive") }
    var lftResult by remember { mutableStateOf(patient.lftResult ?: "Normal") }
    
    var isSavedSuccessfully by remember { mutableStateOf(false) }

    val isXRayOverdue = patient.isXRayOverdue()
    val isEcgOverdue = patient.isEcgOverdue()
    val isBloodTestOverdue = patient.isBloodTestOverdue()
    val isFullyCompliant = patient.isFullyCompliant()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Visual Compliance Status Indicator Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFullyCompliant) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (isFullyCompliant) Color(0xFF4CAF50) else Color(0xFFFF9800)
                ),
                modifier = Modifier.fillMaxWidth().testTag("compliance_status_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isFullyCompliant) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isFullyCompliant) Color(0xFF2E7D32) else Color(0xFFE65100),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isFullyCompliant) "KEPATUHAN SARINGAN: PATUH KKM" else "KEPATUHAN SARINGAN: TERTUNGGAK ⚠️",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isFullyCompliant) Color(0xFF1B5E20) else Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isFullyCompliant) 
                                "Semua ujian saringan diwajibkan telah didaftar dan dikemaskini dalam tempoh sah setahun." 
                                else "Sila kemaskini saringan di bawah untuk mengekalkan pematuhan garis panduan klinikal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isFullyCompliant) Color(0xFF2E7D32) else Color(0xFF5D4037)
                        )
                    }
                }
            }
        }

        // Compliance Breakdown Checkmarks
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Senarai Semak Saringan CPG KKM:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Chest X-Ray (Mandatory for all registered patients)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (!isXRayOverdue) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (!isXRayOverdue) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Chest X-Ray (Wajib Pendaftaran Baru)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (!isXRayOverdue) "Tarikh: $lastXRayDate ($lastXRayResult)" else "⚠️ Tiada rekod X-Ray didaftar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // ECG (Mandatory for Dosage >= 100mg)
                    if (patient.currentDoseMg >= 100.0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (!isEcgOverdue) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (!isEcgOverdue) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Ujian ECG Tahunan (Dos Tinggi ≥ 100mg)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (!isEcgOverdue) "Tarikh: $lastEcgDate ($lastEcgResult - ${if (lastEcgQtcMsText.isNotBlank()) lastEcgQtcMsText + " ms" else "N/A"})" else "⚠️ ECG Tertunggak/Diperlukan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Ujian ECG Tahunan (Khas Dos ≥ 100mg)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "Pengecualian: Dos pesakit semasa ialah ${patient.currentDoseMg.toInt()} mg.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    // Blood Test (Mandatory annually)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (!isBloodTestOverdue) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (!isBloodTestOverdue) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Saringan Darah Tahunan (HIV, Hep B, Hep C, LFT)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (!isBloodTestOverdue) 
                                    "Tarikh: $lastBloodTestDate (HIV: $hivResult, HepB: $hepBResult, HepC: $hepCResult, LFT: $lftResult)" 
                                    else "⚠️ Saringan Darah Tertunggak (>365 hari)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        // Form for entering screening results
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Kemaskini Rekod Saringan & Keputusan:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Chest X-Ray section
                Text("1. Chest X-Ray (Saringan Wajib)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lastXRayDate,
                        onValueChange = { lastXRayDate = it },
                        label = { Text("Tarikh X-Ray (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                    
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text("Keputusan:", style = MaterialTheme.typography.labelSmall)
                        Row {
                            OutlinedButton(
                                onClick = { lastXRayResult = "Normal" },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (lastXRayResult == "Normal") Color(0xFFE8F5E9) else Color.Transparent
                                )
                            ) {
                                Text("Normal", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedButton(
                                onClick = { lastXRayResult = "Abnormal" },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (lastXRayResult == "Abnormal") Color(0xFFFFEBEE) else Color.Transparent
                                )
                            ) {
                                Text("Abnorm", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // ECG section
                Text("2. Ujian ECG Tahunan", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lastEcgDate,
                        onValueChange = { lastEcgDate = it },
                        label = { Text("Tarikh ECG (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = lastEcgQtcMsText,
                        onValueChange = { lastEcgQtcMsText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("QTc (ms)") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Keputusan ECG:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                    Row(modifier = Modifier.weight(1.5f)) {
                        OutlinedButton(
                            onClick = { lastEcgResult = "Normal" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (lastEcgResult == "Normal") Color(0xFFE8F5E9) else Color.Transparent
                            )
                        ) {
                            Text("Normal")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedButton(
                            onClick = { lastEcgResult = "Abnormal" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (lastEcgResult == "Abnormal") Color(0xFFFFEBEE) else Color.Transparent
                            )
                        ) {
                            Text("Abnormal")
                        }
                    }
                }

                // Blood Test section
                Text("3. Saringan Darah Tahunan", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                OutlinedTextField(
                    value = lastBloodTestDate,
                    onValueChange = { lastBloodTestDate = it },
                    label = { Text("Tarikh Ambil Darah (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Sub test details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // HIV
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("HIV Saringan:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                            Row(modifier = Modifier.weight(2.5f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedButton(
                                    onClick = { hivResult = "Non-Reactive" },
                                    modifier = Modifier.weight(1.3f),
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (hivResult == "Non-Reactive") Color(0xFFE8F5E9) else Color.Transparent
                                    )
                                ) {
                                    Text("Non-Reactive", style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(
                                    onClick = { hivResult = "Reactive" },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (hivResult == "Reactive") Color(0xFFFFEBEE) else Color.Transparent
                                    )
                                ) {
                                    Text("Reactive", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // Hep B
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("HBsAg (Hep B):", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                            Row(modifier = Modifier.weight(2.5f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedButton(
                                    onClick = { hepBResult = "Non-Reactive" },
                                    modifier = Modifier.weight(1.3f),
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (hepBResult == "Non-Reactive") Color(0xFFE8F5E9) else Color.Transparent
                                    )
                                ) {
                                    Text("Non-Reactive", style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(
                                    onClick = { hepBResult = "Reactive" },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (hepBResult == "Reactive") Color(0xFFFFEBEE) else Color.Transparent
                                    )
                                ) {
                                    Text("Reactive", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // Hep C
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Anti-HCV (Hep C):", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                            Row(modifier = Modifier.weight(2.5f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedButton(
                                    onClick = { hepCResult = "Non-Reactive" },
                                    modifier = Modifier.weight(1.3f),
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (hepCResult == "Non-Reactive") Color(0xFFE8F5E9) else Color.Transparent
                                    )
                                ) {
                                    Text("Non-Reactive", style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(
                                    onClick = { hepCResult = "Reactive" },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (hepCResult == "Reactive") Color(0xFFFFEBEE) else Color.Transparent
                                    )
                                ) {
                                    Text("Reactive", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // LFT
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("LFT (Fungsi Hati):", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                            Row(modifier = Modifier.weight(2.5f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedButton(
                                    onClick = { lftResult = "Normal" },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (lftResult == "Normal") Color(0xFFE8F5E9) else Color.Transparent
                                    )
                                ) {
                                    Text("Normal", style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(
                                    onClick = { lftResult = "Abnormal" },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (lftResult == "Abnormal") Color(0xFFFFEBEE) else Color.Transparent
                                    )
                                ) {
                                    Text("Abnormal", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Save action
        item {
            if (isSavedSuccessfully) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rekod Saringan Berjaya Disimpan!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    val updated = patient.copy(
                        lastXRayDate = lastXRayDate.ifBlank { null },
                        lastXRayResult = lastXRayResult,
                        lastEcgDate = lastEcgDate.ifBlank { null },
                        lastEcgResult = lastEcgResult,
                        lastEcgQtcMs = lastEcgQtcMsText.toIntOrNull(),
                        lastBloodTestDate = lastBloodTestDate.ifBlank { null },
                        hivResult = hivResult,
                        hepBResult = hepBResult,
                        hepCResult = hepCResult,
                        lftResult = lftResult
                    )
                    onSave(updated)
                    isSavedSuccessfully = true
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_screening_results_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Rekod Saringan KKM", fontWeight = FontWeight.Bold)
            }
        }
    }
}
