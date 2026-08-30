package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DispenseRecord
import com.example.data.model.Patient
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusAmberContainer
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenContainer
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedContainer
import com.example.ui.viewmodel.MethadoneViewModel
import com.example.ui.viewmodel.ScanStatus
import com.example.util.CameraQrScanner
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDispenseScreen(
    viewModel: MethadoneViewModel,
    onNavigateToPatientDetail: (Patient) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scanStatus by viewModel.scanStatus.collectAsStateWithLifecycle()
    val fastTrackMode by viewModel.fastTrackMode.collectAsStateWithLifecycle()
    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val activeOfficer by viewModel.activeOfficerName.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var manualInputText by remember { mutableStateOf("") }
    var isCameraActive by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Bar & Fast Track Mode Switch
            Surface(
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pengimbas Kod QR Harian",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Petugas: $activeOfficer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Fast track toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (fastTrackMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = if (fastTrackMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pantas",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (fastTrackMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = fastTrackMode,
                            onCheckedChange = { viewModel.toggleFastTrackMode(it) },
                            modifier = Modifier.testTag("fast_track_switch")
                        )
                    }
                }
            }

            // Main Scanner Area or Camera Permission Prompt
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (hasCameraPermission && isCameraActive) {
                    CameraQrScanner(
                        onQrCodeDetected = { scannedText ->
                            viewModel.processScannedQr(scannedText, "PENGIMBAS_QR")
                        },
                        isScanningActive = scanStatus is ScanStatus.Idle
                    )
                } else {
                    // Fallback / Permission box
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Color(0xFF53DBC9),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Kamera Diperlukan Untuk Pengimbasan",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Arahkan kamera ke Kod QR Kad Pesakit atau gunakan pilihan carian pantas di bawah.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                modifier = Modifier.testTag("request_camera_button")
                            ) {
                                Text("Berikan Kebenaran Kamera")
                            }
                        }
                    }
                }
            }

            // Bottom Quick Bar & Demo Quick Tap Simulator (Essential for testing & manual input!)
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Manual ID / IC Input Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualInputText,
                            onValueChange = { manualInputText = it },
                            placeholder = { Text("Masukkan No. ID / No. Kad Pengenalan...") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_ic_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (manualInputText.isNotBlank()) {
                                    viewModel.processScannedQr(manualInputText, "CARIAN_MANUAL")
                                    manualInputText = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("search_patient_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Cari")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Semak")
                        }
                    }

                    // Sejarah Imbasan QR Terkini
                    val scanHistory by viewModel.scanHistory.collectAsStateWithLifecycle()
                    if (scanHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sejarah Imbasan Terkini:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "Sentuh untuk semak semula",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.testTag("scan_history_list")
                        ) {
                            items(scanHistory, key = { it.id }) { entry ->
                                Card(
                                    onClick = {
                                        viewModel.processScannedQr(entry.patientId, "SEJARAH_IMBASAN")
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            entry.status.contains("Sedia") || entry.status.contains("Hadir") -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                            entry.status.contains("Sudah") -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                                            else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                        }
                                    ),
                                    modifier = Modifier.testTag("scan_history_item_${entry.id}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            entry.status.contains("Sedia") || entry.status.contains("Hadir") -> StatusGreen
                                                            entry.status.contains("Sudah") -> StatusAmber
                                                            else -> StatusRed
                                                        }
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = entry.timestamp,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = entry.patientName,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${entry.patientId} • ${entry.status}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Demo Simulator Chips (Tap to simulate QR scan of any registered patient)
                    Text(
                        text = "Imbas Ujian / Kad Contoh Pesakit:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(patients) { patient ->
                            Card(
                                onClick = {
                                    viewModel.processScannedQr(patient.toQrPayload(), "KAD_DIGITAL")
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (patient.dispenseType == "TAKE_HOME")
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    else
                                        MaterialTheme.colorScheme.secondaryContainer
                                ),
                                modifier = Modifier.testTag("demo_chip_${patient.patientId}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (patient.dispenseType == "TAKE_HOME")
                                            MaterialTheme.colorScheme.onTertiaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = patient.name.split(" ").firstOrNull() ?: patient.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${patient.currentDoseMg.toInt()}mg (${patient.dispenseType})",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Sheet or Dialog for Dispense Confirmation / Warnings / Success
        when (val status = scanStatus) {
            is ScanStatus.ReadyToDispense -> {
                DispenseConfirmationSheet(
                    patient = status.patient,
                    warningMessage = status.warning,
                    fastTrackMode = fastTrackMode,
                    officerName = activeOfficer,
                    onDismiss = { viewModel.clearScanStatus() },
                    onConfirm = { doseMg, dispenseType, bottlesCount, bottlesReturned, remarks ->
                        viewModel.confirmDispense(
                            patient = status.patient,
                            customDoseMg = doseMg,
                            dispenseType = dispenseType,
                            bottlesCount = bottlesCount,
                            bottlesReturned = bottlesReturned,
                            remarks = remarks,
                            scanMethod = "PENGIMBAS_QR"
                        )
                    }
                )
            }

            is ScanStatus.AlreadyDispensedToday -> {
                AlreadyDispensedAlertSheet(
                    patient = status.patient,
                    record = status.previousRecord,
                    onDismiss = { viewModel.clearScanStatus() },
                    onOverrideDispense = {
                        viewModel.confirmDispense(
                            patient = status.patient,
                            remarks = "DISPENSI KALI KEDUA / DOS TAMBAHAN DILULUSKAN DOKTOR."
                        )
                    }
                )
            }

            is ScanStatus.PatientSuspended -> {
                SuspendedAlertSheet(
                    patient = status.patient,
                    reason = status.reason,
                    onDismiss = { viewModel.clearScanStatus() }
                )
            }

            is ScanStatus.NotFound -> {
                NotFoundSheet(
                    rawCode = status.rawCode,
                    onDismiss = { viewModel.clearScanStatus() }
                )
            }

            is ScanStatus.DispenseSuccess -> {
                DispenseSuccessSheet(
                    patient = status.patient,
                    record = status.record,
                    onDismiss = { viewModel.clearScanStatus() },
                    onViewCard = { onNavigateToPatientDetail(status.patient) }
                )
            }

            is ScanStatus.Idle -> {
                // Do nothing
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispenseConfirmationSheet(
    patient: Patient,
    warningMessage: String?,
    fastTrackMode: Boolean,
    officerName: String,
    onDismiss: () -> Unit,
    onConfirm: (doseMg: Double, dispenseType: String, bottlesCount: Int, bottlesReturned: Boolean, remarks: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedDoseMg by remember { mutableDoubleStateOf(patient.currentDoseMg) }
    var dispenseType by remember { mutableStateOf(patient.dispenseType) }
    var bottlesCount by remember {
        mutableIntStateOf(if (dispenseType == "TAKE_HOME") (if (patient.takeHomeDays > 0) patient.takeHomeDays else 2) else 0)
    }
    var bottlesReturned by remember { mutableStateOf(true) }
    var remarks by remember { mutableStateOf("") }

    val volumeMl = String.format(Locale.US, "%.1f", selectedDoseMg / 5.0).toDouble()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title & Patient ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = patient.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "No. K/P: ${patient.icNumber} • ID: ${patient.patientId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Warning if any
            if (warningMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusAmberContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusAmber
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = warningMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF78350F),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Prescribed Dosage Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DOS PRESKRIPSI METADON",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${selectedDoseMg.toInt()}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = " mg",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "($volumeMl mL Cecair)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = "Kadar Kepekatan Standard: 5 mg / 1 mL",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dispense Type Selector (DOT vs Take-Home)
            Text(
                text = "Kaedah Pengambilan:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    onClick = {
                        dispenseType = "DOT"
                        bottlesCount = 0
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (dispenseType == "DOT") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = null,
                            tint = if (dispenseType == "DOT") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "DOT (Minum Sini)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (dispenseType == "DOT") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    onClick = {
                        dispenseType = "TAKE_HOME"
                        if (bottlesCount == 0) bottlesCount = if (patient.takeHomeDays > 0) patient.takeHomeDays else 2
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (dispenseType == "TAKE_HOME") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = if (dispenseType == "TAKE_HOME") MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bawa Balik (Take-Home)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (dispenseType == "TAKE_HOME") MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Take Home options if applicable
            if (dispenseType == "TAKE_HOME") {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Bilangan Botol Bawa Balik: ${bottlesCount.coerceAtMost(6)} hari (${bottlesCount.coerceAtMost(6)} botol)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Had Maksimum Bekalan Bawa Balik (Take-Home): 6 botol sahaja mengikut CPG KKM.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = bottlesCount.coerceAtMost(6).toFloat(),
                            onValueChange = { bottlesCount = it.toInt().coerceAtMost(6) },
                            valueRange = 1f..6f,
                            steps = 4,
                            modifier = Modifier.testTag("bottles_slider")
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { bottlesReturned = !bottlesReturned }
                        ) {
                            Checkbox(
                                checked = bottlesReturned,
                                onCheckedChange = { bottlesReturned = it },
                                modifier = Modifier.testTag("bottles_return_checkbox")
                            )
                            Text(
                                text = "Botol kosong sebelum ini telah dipulangkan dan diperiksa",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Officer & Notes
            Text(
                text = "Doktor Bertanggungjawab: ${patient.doctorName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "Petugas Dispensasi: $officerName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = {
                    onConfirm(selectedDoseMg, dispenseType, bottlesCount, bottlesReturned, remarks)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_dispense_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sahkan & Rekod Dispensasi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Batal")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlreadyDispensedAlertSheet(
    patient: Patient,
    record: DispenseRecord,
    onDismiss: () -> Unit,
    onOverrideDispense: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showOverrideConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(StatusAmberContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = StatusAmber,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PESAKIT SUDAH MENGAMBIL UBAT HARI INI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF92400E),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${patient.name} (${patient.icNumber}) telah direkodkan mengambil dos methadone untuk tarikh hari ini.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Maklumat Dispensasi Terdahulu:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• Masa Pengambilan: ${record.dispenseTime} (${record.dispenseDate})", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Dos Diberikan: ${record.doseMg.toInt()} mg (${record.doseVolumeMl} mL)", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Kaedah: ${record.dispenseType} (Dispensasi oleh: ${record.officerName})", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Catatan: ${record.remarks}", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("dismiss_already_dispensed_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Faham & Selesai")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!showOverrideConfirm) {
                OutlinedButton(
                    onClick = { showOverrideConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Kebenaran Khas Doktor (Override Dos)")
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusRedContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "AMARAN KESELAMATAN: Sila pastikan arahan bertulis doktor diperolehi sebelum merekodkan dos tambahan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onOverrideDispense()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sahkan Dos Tambahan")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuspendedAlertSheet(
    patient: Patient,
    reason: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(StatusRedContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = StatusRed,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "STATUS PESAKIT: ${patient.status}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = StatusRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tutup")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotFoundSheet(
    rawCode: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Pesakit Tidak Dijumpai",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Kod '$rawCode' tidak sepadan dengan mana-mana pesakit berdaftar dalam sistem Klinik Kesihatan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cuba Lagi")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispenseSuccessSheet(
    patient: Patient,
    record: DispenseRecord,
    onDismiss: () -> Unit,
    onViewCard: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(StatusGreenContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = StatusGreen,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DISPENSI BERJAYA DIREKODKAN!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = StatusGreen
            )

            Text(
                text = "Kehadiran pesakit telah dikemas kini secara automatik.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = patient.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "No. K/P: ${patient.icNumber} • ID: ${patient.patientId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dos Diberikan:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${record.doseMg.toInt()} mg (${record.doseVolumeMl} mL)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Kaedah:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (record.dispenseType == "DOT") "DOT (Minum di Klinik)" else "Bawa Balik (${record.takeHomeBottlesCount} Botol)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Masa:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${record.dispenseTime}, ${record.dispenseDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("dismiss_success_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Imbas Pesakit Seterusnya",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    onDismiss()
                    onViewCard()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lihat Kad QR Digital Pesakit")
            }
        }
    }
}
