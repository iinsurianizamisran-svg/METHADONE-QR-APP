package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import com.example.ui.screens.ClinicSetupScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MissedDoseAlertBanner
import com.example.ui.screens.MissedDoseNotificationModal
import com.example.data.model.Patient
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.PatientDetailDialog
import com.example.ui.screens.PatientsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ScanDispenseScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.MethadoneViewModel

enum class MainNavigationTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    SCAN("Imbasan QR", Icons.Default.QrCodeScanner, "tab_scan"),
    ATTENDANCE("Kehadiran", Icons.Default.FactCheck, "tab_attendance"),
    PATIENTS("Pesakit", Icons.Default.People, "tab_patients"),
    INVENTORY("Inventori", Icons.Default.LocalHospital, "tab_inventory"),
    REPORTS("Laporan", Icons.Default.Assessment, "tab_reports")
}

class MainActivity : ComponentActivity() {
    private val viewModel: MethadoneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                val clinicSettings by viewModel.clinicSettings.collectAsStateWithLifecycle()
                var forceShowSetup by remember { mutableStateOf(false) }

                if (!isLoggedIn) {
                    LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = {
                            // Check if setup completed
                        }
                    )
                } else if (clinicSettings?.isSetupCompleted == false || forceShowSetup) {
                    ClinicSetupScreen(
                        viewModel = viewModel,
                        onSetupComplete = {
                            forceShowSetup = false
                        }
                    )
                } else {
                    MainAppScreen(
                        viewModel = viewModel,
                        onOpenSetup = { forceShowSetup = true }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: MethadoneViewModel,
    onOpenSetup: () -> Unit = {}
) {
    var currentTab by remember { mutableStateOf(MainNavigationTab.SCAN) }
    var selectedPatientForDetail by remember { mutableStateOf<Patient?>(null) }
    var showOfficerDialog by remember { mutableStateOf(false) }
    var showMissedDoseModal by remember { mutableStateOf(false) }

    val activeOfficer by viewModel.activeOfficerName.collectAsStateWithLifecycle()
    val summary by viewModel.attendanceSummary.collectAsStateWithLifecycle()
    val isLowStockAlert by viewModel.isLowStockAlert.collectAsStateWithLifecycle()
    val missedDoseAlerts by viewModel.missedDoseAlerts.collectAsStateWithLifecycle()
    val clinicSettings by viewModel.clinicSettings.collectAsStateWithLifecycle()

    val clinicDisplayName = clinicSettings?.clinicName ?: "e-Methadone PKD Kluang"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "e-Methadone QR",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = clinicDisplayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenSetup,
                        modifier = Modifier.testTag("clinic_setup_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Tetapan Klinik",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showMissedDoseModal = true },
                        modifier = Modifier.testTag("notification_center_button")
                    ) {
                        if (missedDoseAlerts.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = StatusRed,
                                        contentColor = Color.White
                                    ) {
                                        Text("${missedDoseAlerts.size}")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Notifikasi Cicir Dos",
                                    tint = StatusRed
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifikasi Staf",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { showOfficerDialog = true },
                        modifier = Modifier.testTag("officer_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Tukar Petugas",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                MainNavigationTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            if (tab == MainNavigationTab.ATTENDANCE && summary.pendingCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = Color.White
                                        ) {
                                            Text("${summary.pendingCount}")
                                        }
                                    }
                                ) {
                                    Icon(imageVector = tab.icon, contentDescription = tab.title)
                                }
                            } else if (tab == MainNavigationTab.INVENTORY && isLowStockAlert) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = StatusRed,
                                            contentColor = Color.White
                                        ) {
                                            Text("!")
                                        }
                                    }
                                ) {
                                    Icon(imageVector = tab.icon, contentDescription = tab.title)
                                }
                            } else {
                                Icon(imageVector = tab.icon, contentDescription = tab.title)
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (missedDoseAlerts.isNotEmpty()) {
                MissedDoseAlertBanner(
                    alertPatients = missedDoseAlerts,
                    onViewAlertsClick = { showMissedDoseModal = true },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    MainNavigationTab.SCAN -> {
                        ScanDispenseScreen(
                            viewModel = viewModel,
                            onNavigateToPatientDetail = { patient ->
                                selectedPatientForDetail = patient
                            }
                        )
                    }

                    MainNavigationTab.ATTENDANCE -> {
                        AttendanceScreen(
                            viewModel = viewModel,
                            onPatientClick = { patient ->
                                selectedPatientForDetail = patient
                            }
                        )
                    }

                    MainNavigationTab.PATIENTS -> {
                        PatientsScreen(
                            viewModel = viewModel,
                            onPatientClick = { patient ->
                                selectedPatientForDetail = patient
                            },
                            onNavigateToScan = {
                                currentTab = MainNavigationTab.SCAN
                            }
                        )
                    }

                    MainNavigationTab.INVENTORY -> {
                        InventoryScreen(viewModel = viewModel)
                    }

                    MainNavigationTab.REPORTS -> {
                        ReportsScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // Staff Notification Modal for Missed Dose (>3 days)
        if (showMissedDoseModal) {
            MissedDoseNotificationModal(
                alertPatients = missedDoseAlerts,
                viewModel = viewModel,
                onDismiss = { showMissedDoseModal = false },
                onNavigateToScanForPatient = { patient ->
                    currentTab = MainNavigationTab.SCAN
                }
            )
        }

        // Selected Patient Details & Digital QR Card Modal
        selectedPatientForDetail?.let { patient ->
            PatientDetailDialog(
                patient = patient,
                viewModel = viewModel,
                onDismiss = { selectedPatientForDetail = null },
                onDirectDispense = {
                    selectedPatientForDetail = null
                    currentTab = MainNavigationTab.SCAN
                    viewModel.processScannedQr(patient.toQrPayload(), "KAD_DIGITAL")
                }
            )
        }

        // Officer Switcher Dialog
        if (showOfficerDialog) {
            OfficerSwitcherDialog(
                currentOfficer = activeOfficer,
                onDismiss = { showOfficerDialog = false },
                onSelectOfficer = { newOfficer ->
                    viewModel.updateOfficerName(newOfficer)
                    showOfficerDialog = false
                },
                onLogout = {
                    showOfficerDialog = false
                    viewModel.logout()
                }
            )
        }
    }
}

@Composable
fun OfficerSwitcherDialog(
    currentOfficer: String,
    onDismiss: () -> Unit,
    onSelectOfficer: (String) -> Unit,
    onLogout: () -> Unit
) {
    val officersList = listOf(
        "Jururawat Kanan Siti (Farmasi MMT)",
        "Pegawai Farmasi Azman bin Daud",
        "Jururawat Masyarakat Farah",
        "Pembantu Perawatan Kesihatan (PPK) Hafiz",
        "Dr. Farah Hanim (Pakar Perubatan Keluarga)"
    )

    var customOfficerName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profil / Tukar Petugas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Keluar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Petugas Aktif: $currentOfficer",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Pilih nama petugas bertugas untuk rekod dispensasi:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                officersList.forEach { officer ->
                    Card(
                        onClick = { onSelectOfficer(officer) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (officer == currentOfficer)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = officer,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (officer == currentOfficer) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = customOfficerName,
                    onValueChange = { customOfficerName = it },
                    placeholder = { Text("Nama Petugas Lain...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            if (customOfficerName.isNotBlank()) {
                TextButton(onClick = { onSelectOfficer(customOfficerName.trim()) }) {
                    Text("Gunakan Nama Ini")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
