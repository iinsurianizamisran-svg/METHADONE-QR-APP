package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DispenseRecord
import com.example.data.model.Patient
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusAmberContainer
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusBlueContainer
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenContainer
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedContainer
import com.example.ui.viewmodel.MethadoneViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: MethadoneViewModel,
    onPatientClick: (Patient) -> Unit,
    modifier: Modifier = Modifier
) {
    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val dateRecords by viewModel.dateRecords.collectAsStateWithLifecycle()
    val summary by viewModel.attendanceSummary.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val activeOfficer by viewModel.activeOfficerName.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("SEMUA") } // SEMUA, HADIR, BELUM_HADIR, DOT, TAKE_HOME
    var showExportModal by remember { mutableStateOf(false) }

    if (showExportModal) {
        ExportModal(
            selectedDate = selectedDate,
            dateRecords = dateRecords,
            allRecords = allRecords,
            summary = summary,
            officerName = activeOfficer,
            onDismiss = { showExportModal = false }
        )
    }

    // Compute patient attendance states for the selected date
    val recordsByPatientId = remember(dateRecords) {
        dateRecords.associateBy { it.patientId }
    }

    val filteredList = remember(patients, recordsByPatientId, searchQuery, selectedFilter) {
        patients.filter { patient ->
            val matchesSearch = searchQuery.isBlank() ||
                    patient.name.contains(searchQuery, ignoreCase = true) ||
                    patient.icNumber.contains(searchQuery, ignoreCase = true) ||
                    patient.patientId.contains(searchQuery, ignoreCase = true)

            val hasAttended = recordsByPatientId.containsKey(patient.patientId)

            val matchesFilter = when (selectedFilter) {
                "HADIR" -> hasAttended
                "BELUM_HADIR" -> !hasAttended
                "DOT" -> patient.dispenseType == "DOT"
                "TAKE_HOME" -> patient.dispenseType == "TAKE_HOME"
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Date Selector Header
        item {
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
                    IconButton(
                        onClick = {
                            val prevDate = changeDateByDays(selectedDate, -1)
                            viewModel.setSelectedDate(prevDate)
                        }
                    ) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Semalam", modifier = Modifier.size(18.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedDate == MethadoneViewModel.getTodayDateString()) "Hari Ini ($selectedDate)" else selectedDate,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val nextDate = changeDateByDays(selectedDate, 1)
                                viewModel.setSelectedDate(nextDate)
                            }
                        ) {
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Esok", modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { showExportModal = true },
                            modifier = Modifier.testTag("attendance_export_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Eksport Laporan",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Offline queue widget
        item {
            OfflineQueueWidget(viewModel = viewModel)
        }

        // Clinical follow up widget
        item {
            ClinicalFollowUpWidget(
                viewModel = viewModel,
                patients = patients,
                userRole = userRole,
                onPatientClick = onPatientClick
            )
        }

        // Daily attendance chart
        item {
            DailyAttendanceChart(
                attended = summary.attendedCount,
                total = summary.totalRegistered,
                missed = summary.missedCount
            )
        }

        // Smart notifications widget
        item {
            SmartNotificationWidget(
                patients = patients,
                recordsByPatientId = recordsByPatientId
            )
        }

        // 4 Summary Metrics Cards
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: Hadir
                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = StatusGreenContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "HADIR & DISPENSI",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${summary.attendedCount} / ${summary.totalRegistered}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF065F46)
                            )
                            val percentage = if (summary.totalRegistered > 0)
                                (summary.attendedCount * 100) / summary.totalRegistered
                            else 0
                            Text(
                                text = "$percentage% Selesai",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Card 2: Menunggu
                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = StatusAmberContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "BELUM HADIR",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = StatusAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${summary.pendingCount}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "Menunggu Giliran",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Card 3: Stock & Volume Dispensed Today
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Jumlah Metadon Dikeluarkan Hari Ini",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${summary.totalMgDispensed.toInt()} mg",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(${String.format(Locale.US, "%.1f", summary.totalMlDispensed)} mL Cecair)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "DOT: ${summary.dotCount} org",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Bawa Balik: ${summary.takeHomeCount} org",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Search Bar & Filter Chips
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama, IC, atau ID pesakit...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("attendance_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "SEMUA" to "Semua (${patients.size})",
                        "HADIR" to "Hadir (${summary.attendedCount})",
                        "BELUM_HADIR" to "Belum Hadir (${summary.pendingCount})",
                        "DOT" to "DOT di Klinik",
                        "TAKE_HOME" to "Bawa Balik"
                    )
                    items(filters) { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Patient Attendance Records List
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tiada rekod pesakit yang sepadan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { it.patientId }) { patient ->
                val dispenseRecord = recordsByPatientId[patient.patientId]
                val isAttended = dispenseRecord != null

                AttendancePatientCard(
                    patient = patient,
                    record = dispenseRecord,
                    isAttended = isAttended,
                    onCardClick = { onPatientClick(patient) },
                    onQuickDispense = {
                        viewModel.processScannedQr(patient.toQrPayload(), "KAD_DIGITAL")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun AttendancePatientCard(
    patient: Patient,
    record: DispenseRecord?,
    isAttended: Boolean,
    onCardClick: () -> Unit,
    onQuickDispense: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onCardClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAttended) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.testTag("patient_card_${patient.patientId}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name & ID
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = patient.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "IC: ${patient.icNumber} • ${patient.patientId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Status Badge (HADIR vs BELUM HADIR)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isAttended) StatusGreenContainer else StatusAmberContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isAttended) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = if (isAttended) StatusGreen else StatusAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAttended) "HADIR" else "BELUM HADIR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAttended) Color(0xFF065F46) else Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details row (Dose, Mode, Time)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${patient.currentDoseMg.toInt()} mg (${patient.doseVolumeMl} mL)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (patient.dispenseType == "TAKE_HOME") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (patient.dispenseType == "TAKE_HOME") "Bawa Balik (${patient.takeHomeDays}h)" else "DOT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (patient.dispenseType == "TAKE_HOME") MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                if (isAttended && record != null) {
                    Text(
                        text = "Masa: ${record.dispenseTime}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    OutlinedButton(
                        onClick = onQuickDispense,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("quick_dispense_${patient.patientId}")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dispensasi", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

private fun changeDateByDays(dateStr: String, days: Int): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = sdf.parse(dateStr) ?: Date()
        cal.add(Calendar.DAY_OF_YEAR, days)
        sdf.format(cal.time)
    } catch (e: Exception) {
        dateStr
    }
}

@Composable
fun DailyAttendanceChart(attended: Int, total: Int, missed: Int) {
    val completedPercentage = if (total > 0) (attended * 100) / total else 0
    val missedPercentage = if (total > 0) (missed * 100) / total else 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Statistik Visual Kehadiran Harian",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Circular Canvas Chart
                Canvas(modifier = Modifier.size(80.dp)) {
                    val strokeWidth = 14f
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                    
                    // Draw Gray background ring
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        radius = radius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                    )

                    // Draw Attended arc (Green)
                    val sweepAngleAttended = if (total > 0) (attended.toFloat() / total.toFloat()) * 360f else 0f
                    drawArc(
                        color = Color(0xFF10B981), // Emerald Green
                        startAngle = -90f,
                        sweepAngle = sweepAngleAttended,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )

                    // Draw Missed arc (Red)
                    if (missed > 0) {
                        val sweepAngleMissed = (missed.toFloat() / total.toFloat()) * 360f
                        drawArc(
                            color = Color(0xFFEF4444), // Crimson Red
                            startAngle = -90f + sweepAngleAttended,
                            sweepAngle = sweepAngleMissed,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeWidth,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }
                }

                // Legend and Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF10B981), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Selesai (Hadir)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                        Text("$completedPercentage%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFFEF4444), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Belum Hadir (Cicir)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                        Text("$missedPercentage%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Kehadiran: $attended dari $total pesakit berdaftar.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun OfflineQueueWidget(viewModel: MethadoneViewModel) {
    val isOffline by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val unsyncedCount = remember(allRecords) { allRecords.count { !it.isSynced } }
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOffline) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isOffline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = if (isOffline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sistem Luar Talian (Offline Queue)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Switch(
                    checked = isOffline,
                    onCheckedChange = { viewModel.toggleOfflineMode(it) },
                    modifier = Modifier.testTag("offline_mode_toggle_switch")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (isOffline) {
                Text(
                    text = "Aplikasi berada dalam mod LUAR TALIAN. Semua imbasan QR kehadiran disimpan secara tempatan ke pangkalan data Room dan diletakkan dalam baris gilir (Queue).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = "Aplikasi berada dalam mod DALAM TALIAN. Data disegerakkan terus ke server KKM.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (unsyncedCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$unsyncedCount imbasan belum disegerakkan",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    TextButton(
                        onClick = {
                            viewModel.syncDatabase { success, message ->
                                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("sync_offline_queue_button")
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Segerakkan Sekarang", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ClinicalFollowUpWidget(
    viewModel: MethadoneViewModel,
    patients: List<Patient>,
    userRole: String,
    onPatientClick: (Patient) -> Unit
) {
    // Show clinical alerts for Doctor, Farmasi or Admin
    if (userRole != com.example.data.model.UserRoles.DOCTOR &&
        userRole != com.example.data.model.UserRoles.PHARMACY &&
        userRole != com.example.data.model.UserRoles.ADMIN) {
        return
    }

    val overduePatients = remember(patients) {
        patients.filter { it.isXRayOverdue() || it.isEcgOverdue() || it.isBloodTestOverdue() }
    }
    val pendingDosePatients = remember(patients) {
        patients.filter { it.pendingDoseIncreaseRequestMg != null }
    }

    val totalAlerts = overduePatients.size + pendingDosePatients.size
    if (totalAlerts == 0) return

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StatusAmberContainer),
        border = BorderStroke(1.dp, StatusAmber),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { isExpanded = !isExpanded }
            .testTag("clinical_followup_summary_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tindakan Susulan Klinikal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$totalAlerts Kes",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Terdapat $totalAlerts perkara klinikal penting yang memerlukan tindakan segera Pegawai Perubatan atau Farmasi.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB45309)
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF59E0B).copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                // Dosage Increase requests section
                if (pendingDosePatients.isNotEmpty()) {
                    Text(
                        text = "Permohonan Peningkatan Dos (${pendingDosePatients.size} kes)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    pendingDosePatients.forEach { pat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPatientClick(pat) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${pat.name} (Syor: ${pat.pendingDoseIncreaseRequestMg?.toInt()}mg)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF0F172A)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color(0xFF92400E),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Overdue Screening section
                if (overduePatients.isNotEmpty()) {
                    Text(
                        text = "Saringan / ECG Terlepas (${overduePatients.size} pesakit)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    overduePatients.forEach { pat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPatientClick(pat) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val items = mutableListOf<String>()
                            if (pat.isXRayOverdue()) items.add("X-Ray")
                            if (pat.isEcgOverdue()) items.add("ECG")
                            if (pat.isBloodTestOverdue()) items.add("Ujian Darah")
                            
                            Text(
                                text = "• ${pat.name} (Ralat: ${items.joinToString(", ")})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF0F172A)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color(0xFF92400E),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Klik untuk lihat senarai terperinci...",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFD97706),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SmartNotificationWidget(
    patients: List<Patient>,
    recordsByPatientId: Map<String, DispenseRecord>
) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)

    // Compute missing check-ins
    val notCheckedInPatients = remember(patients, recordsByPatientId) {
        patients.filter { !recordsByPatientId.containsKey(it.patientId) && it.status == "AKTIF" }
    }

    if (notCheckedInPatients.isEmpty()) return

    // Show warning if it is late in the day (e.g. after 11:00 AM or 12:00 PM)
    val checkInTimeLimitHour = 12
    val isAfterLimit = hour >= checkInTimeLimitHour

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isAfterLimit) StatusRedContainer else MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isAfterLimit) StatusRed else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAfterLimit) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (isAfterLimit) StatusRed else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sistem Amaran Kehadiran KKM",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isAfterLimit) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isAfterLimit) Color(0xFFEF4444) else MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isAfterLimit) "AMARAN LEWAT" else "PERINGATAN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isAfterLimit) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (isAfterLimit) {
                Text(
                    text = "AMARAN: Jam sudah melepasi jam ${checkInTimeLimitHour}:00 PM. Seramai ${notCheckedInPatients.size} pesakit berisiko CICIR dos rawatan harian sekiranya tidak hadir hari ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF991B1B),
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "Peringatan jadual harian: ${notCheckedInPatients.size} pesakit dijadualkan menerima metadon hari ini namun masih belum mendaftar masuk.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notCheckedInPatients.take(5)) { pat ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAfterLimit) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (isAfterLimit) Color(0xFFFECACA) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = pat.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isAfterLimit) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "Dos: ${pat.currentDoseMg.toInt()}mg • ${pat.phoneNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
