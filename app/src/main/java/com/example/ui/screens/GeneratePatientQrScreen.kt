package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Patient
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenContainer
import com.example.ui.viewmodel.MethadoneViewModel
import com.example.util.QrCodeUtil
import java.util.Locale

@Composable
fun GeneratePatientQrScreen(
    viewModel: MethadoneViewModel,
    onNavigateToScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Daftar & Jana QR Baharu, 1: Pilih Pesakit Sedia Ada
    val patients by viewModel.patients.collectAsStateWithLifecycle()

    var newlyCreatedPatientForDialog by remember { mutableStateOf<Patient?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Penjana Kod QR Pesakit (Admin)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Jana Kad QR Unik Terapi Gentian Metadon",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Pendaftaran & Penjanaan QR") },
                        icon = { Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_new_patient_qr")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Pilih Pesakit (${patients.size})") },
                        icon = { Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_existing_patient_qr")
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                NewPatientQrGeneratorView(
                    viewModel = viewModel,
                    onPatientRegistered = { patient ->
                        newlyCreatedPatientForDialog = patient
                        showSuccessDialog = true
                    },
                    onNavigateToScan = onNavigateToScan
                )
            }
            1 -> {
                ExistingPatientsQrView(
                    patients = patients,
                    viewModel = viewModel,
                    onNavigateToScan = onNavigateToScan
                )
            }
        }
    }

    // Success Dialog displaying the newly created unique QR code
    if (showSuccessDialog && newlyCreatedPatientForDialog != null) {
        GeneratedQrResultModal(
            patient = newlyCreatedPatientForDialog!!,
            onDismiss = {
                showSuccessDialog = false
                newlyCreatedPatientForDialog = null
            },
            onTestScan = {
                viewModel.processScannedQr(newlyCreatedPatientForDialog!!.toQrPayload())
                showSuccessDialog = false
                newlyCreatedPatientForDialog = null
                onNavigateToScan()
            }
        )
    }
}

@Composable
fun NewPatientQrGeneratorView(
    viewModel: MethadoneViewModel,
    onPatientRegistered: (Patient) -> Unit,
    onNavigateToScan: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var icNumber by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("01") }
    var doseMgText by remember { mutableStateOf("60") }
    var dispenseType by remember { mutableStateOf("DOT") }
    var takeHomeDaysText by remember { mutableStateOf("2") }
    var doctorName by remember { mutableStateOf("Dr. Farah Hanim (FMS)") }
    var clinicLocation by remember { mutableStateOf("Klinik Kesihatan Cheras") }
    var notes by remember { mutableStateOf("Pesakit patuh rawatan harian Metadon.") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Real-time calculated preview fields
    val parsedDose = doseMgText.toDoubleOrNull() ?: 60.0
    val calculatedVolumeMl = remember(parsedDose) {
        String.format(Locale.US, "%.1f", parsedDose / 5.0).toDouble()
    }

    // Live generated preview payload & QR Bitmap
    val previewPatientId = remember(name, icNumber) {
        val cleanName = name.trim().replace(" ", "").take(3).uppercase()
        val suffix = if (cleanName.isNotBlank()) cleanName else "NEW"
        "METH-2026-${(1000..9999).random()}"
    }

    val livePayload = remember(previewPatientId, icNumber, name, parsedDose, dispenseType) {
        val safeName = if (name.isNotBlank()) name.trim() else "NAMA PESAKIT"
        val safeIc = if (icNumber.isNotBlank()) icNumber.trim() else "880512-10-5543"
        "METH_QR|$previewPatientId|$safeIc|$safeName|$parsedDose|$dispenseType"
    }

    val liveQrBitmap = remember(livePayload) {
        QrCodeUtil.generateQrBitmap(livePayload, 600)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic Live QR Preview Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_qr_preview_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PRATONTON KOD QR SEGERA",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ID: $previewPatientId",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (liveQrBitmap != null) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = liveQrBitmap.asImageBitmap(),
                                contentDescription = "Pratonton Kod QR",
                                modifier = Modifier.size(176.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (name.isNotBlank()) name else "Nama Pesakit Baru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "IC: ${if (icNumber.isNotBlank()) icNumber else "No. K/P Sila diisi"} • Dos: ${parsedDose.toInt()} mg (${calculatedVolumeMl} mL)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // QR Payload Details Contract Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Payload Unik Kod QR (Format Standard MMT KKM):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = livePayload,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Form Section
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Maklumat Pendaftaran Pesakit Baharu",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Penuh Pesakit *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = icNumber,
                        onValueChange = { icNumber = it },
                        label = { Text("No. Kad Pengenalan (cth: 880512-10-5543) *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_ic_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("No. Telefon / Waris") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = doseMgText,
                            onValueChange = { doseMgText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Dos Metadon (mg) *") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_dose_input")
                        )

                        OutlinedTextField(
                            value = "${calculatedVolumeMl} mL",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Isi Padu (mL)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Kaedah Dispensasi:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { dispenseType = "DOT" },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_dispense_dot_button"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (dispenseType == "DOT") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Text("DOT (Klinik)")
                        }

                        OutlinedButton(
                            onClick = { dispenseType = "TAKE_HOME" },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_dispense_takehome_button"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (dispenseType == "TAKE_HOME") MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent
                            )
                        ) {
                            Text("Bawa Balik")
                        }
                    }

                    if (dispenseType == "TAKE_HOME") {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = takeHomeDaysText,
                            onValueChange = { takeHomeDaysText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Kelayakan Hari Bawa Balik (1 - 7 hari)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = doctorName,
                        onValueChange = { doctorName = it },
                        label = { Text("Doktor / Pakar Penilai") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = clinicLocation,
                        onValueChange = { clinicLocation = it },
                        label = { Text("Lokasi Klinik Kesihatan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Klinikal") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submission & Generator Action Button
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Sila masukkan nama penuh pesakit."
                                return@Button
                            }
                            if (icNumber.isBlank()) {
                                errorMessage = "Sila masukkan No. Kad Pengenalan pesakit."
                                return@Button
                            }
                            val dose = doseMgText.toDoubleOrNull() ?: 0.0
                            if (dose <= 0) {
                                errorMessage = "Sila masukkan dos preskripsi yang sah."
                                return@Button
                            }

                            val days = takeHomeDaysText.toIntOrNull() ?: 0

                            viewModel.addNewPatient(
                                name = name,
                                icNumber = icNumber,
                                phone = phoneNumber,
                                doseMg = dose,
                                dispenseType = dispenseType,
                                takeHomeDays = days,
                                doctor = doctorName,
                                clinic = clinicLocation,
                                notes = notes,
                                onSuccess = { newlyCreated ->
                                    onPatientRegistered(newlyCreated)
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_and_generate_qr_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daftar Pesakit & Jana Kod QR Unik",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExistingPatientsQrView(
    patients: List<Patient>,
    viewModel: MethadoneViewModel,
    onNavigateToScan: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPatient by remember { mutableStateOf<Patient?>(patients.firstOrNull()) }

    val filteredPatients = remember(patients, searchQuery) {
        if (searchQuery.isBlank()) patients
        else patients.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.icNumber.contains(searchQuery, ignoreCase = true) ||
            it.patientId.contains(searchQuery, ignoreCase = true)
        }
    }

    val selectedQrBitmap = remember(selectedPatient) {
        selectedPatient?.let { QrCodeUtil.generateQrBitmap(it.toQrPayload(), 600) }
    }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search & Selector Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari pesakit berdaftar untuk memaparkan QR...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("existing_patient_search_input")
        )

        if (selectedPatient != null && selectedQrBitmap != null) {
            // High Polish MOH Digital ID Badge Preview Card
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("existing_patient_qr_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "KEMENTERIAN KESIHATAN MALAYSIA",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = selectedPatient!!.clinicLocation,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
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
                                text = selectedPatient!!.status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = selectedQrBitmap.asImageBitmap(),
                            contentDescription = "Kod QR Pesakit",
                            modifier = Modifier.size(176.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = selectedPatient!!.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "No. K/P: ${selectedPatient!!.icNumber} • ID: ${selectedPatient!!.patientId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Dos Preskripsi: ${selectedPatient!!.currentDoseMg.toInt()} mg (${selectedPatient!!.doseVolumeMl} mL) • Kaedah: ${if (selectedPatient!!.dispenseType == "TAKE_HOME") "Bawa Balik" else "DOT"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Admin Actions Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(selectedPatient!!.toQrPayload()))
                                Toast.makeText(context, "Payload Kod QR telah disalin!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_payload_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin Payload", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Kad Digital QR ${selectedPatient!!.name} berjaya dicipta untuk dicetak!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("print_qr_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cetak Kad", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                viewModel.processScannedQr(selectedPatient!!.toQrPayload())
                                onNavigateToScan()
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("test_scan_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Uji Imbas", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        Text(
            text = "Senarai Pesakit Untuk Penjanaan QR:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        // Selectable Patient List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredPatients, key = { it.patientId }) { patient ->
                val isSelected = selectedPatient?.patientId == patient.patientId
                Card(
                    onClick = { selectedPatient = patient },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("select_patient_item_${patient.patientId}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = patient.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ID: ${patient.patientId} • IC: ${patient.icNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratedQrResultModal(
    patient: Patient,
    onDismiss: () -> Unit,
    onTestScan: () -> Unit
) {
    val qrBitmap = remember(patient) {
        QrCodeUtil.generateQrBitmap(patient.toQrPayload(), 600)
    }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(StatusGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Pendaftaran Berjaya!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kod QR Unik Pesakit Telah Dijana",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (qrBitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, StatusGreen, RoundedCornerShape(16.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Kod QR Pesakit Baharu",
                            modifier = Modifier.size(170.dp)
                        )
                    }
                }

                Text(
                    text = patient.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "ID Rasmi: ${patient.patientId}\nNo. K/P: ${patient.icNumber}\nDos Preskripsi: ${patient.currentDoseMg.toInt()} mg (${patient.doseVolumeMl} mL)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Payload Encoded:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = patient.toQrPayload(),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onTestScan,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("dialog_test_scan_button")
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Uji Imbas Dalam Sistem")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(patient.toQrPayload()))
                    Toast.makeText(context, "Payload disalin!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("dialog_copy_payload_button")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Salin Payload")
            }
        }
    )
}
