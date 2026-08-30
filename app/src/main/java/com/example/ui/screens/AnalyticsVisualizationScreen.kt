package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenContainer
import com.example.ui.viewmodel.MethadoneViewModel
import java.util.Locale

// Data model for Monthly Trend Visualization
data class MonthlyTrendData(
    val month: String,
    val year: Int,
    val dotAttendance: Int,
    val takeHomeAttendance: Int,
    val totalVolumeMl: Double,
    val totalDoseMg: Double,
    val complianceRate: Double
) {
    val totalAttendance: Int get() = dotAttendance + takeHomeAttendance
    val totalVolumeLiters: Double get() = totalVolumeMl / 1000.0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsVisualizationScreen(
    viewModel: MethadoneViewModel,
    modifier: Modifier = Modifier
) {
    // 8-Month Historical Data for MMT Clinic Analytics
    val monthlyDataList = remember {
        listOf(
            MonthlyTrendData("Jan", 2026, dotAttendance = 310, takeHomeAttendance = 110, totalVolumeMl = 8400.0, totalDoseMg = 42000.0, complianceRate = 91.5),
            MonthlyTrendData("Feb", 2026, dotAttendance = 325, takeHomeAttendance = 125, totalVolumeMl = 9000.0, totalDoseMg = 45000.0, complianceRate = 92.0),
            MonthlyTrendData("Mac", 2026, dotAttendance = 340, takeHomeAttendance = 140, totalVolumeMl = 9600.0, totalDoseMg = 48000.0, complianceRate = 93.2),
            MonthlyTrendData("Apr", 2026, dotAttendance = 360, takeHomeAttendance = 150, totalVolumeMl = 10200.0, totalDoseMg = 51000.0, complianceRate = 92.8),
            MonthlyTrendData("Mei", 2026, dotAttendance = 375, takeHomeAttendance = 155, totalVolumeMl = 10600.0, totalDoseMg = 53000.0, complianceRate = 93.8),
            MonthlyTrendData("Jun", 2026, dotAttendance = 390, takeHomeAttendance = 170, totalVolumeMl = 11200.0, totalDoseMg = 56000.0, complianceRate = 94.0),
            MonthlyTrendData("Jul", 2026, dotAttendance = 420, takeHomeAttendance = 190, totalVolumeMl = 12200.0, totalDoseMg = 61000.0, complianceRate = 94.5),
            MonthlyTrendData("Ogos", 2026, dotAttendance = 440, takeHomeAttendance = 200, totalVolumeMl = 12800.0, totalDoseMg = 64000.0, complianceRate = 95.2)
        )
    }

    var selectedMonthIndex by remember { mutableIntStateOf(monthlyDataList.size - 1) }
    var chartViewType by remember { mutableIntStateOf(0) } // 0: Attendance Trend, 1: Volume & Medication Dispensed
    var selectedCategoryFilter by remember { mutableStateOf("SEMUA") } // SEMUA, DOT, TAKE_HOME

    val activeMonthData = monthlyDataList.getOrElse(selectedMonthIndex) { monthlyDataList.last() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Visualization Header Banner
        item {
            Surface(
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Visualisasi Analytics & Trend",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Trend Kehadiran Pesakit & Pengeluaran Metadon Bulanan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode Toggle: Attendance vs Medication Dispensed Chart
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = chartViewType == 0,
                            onClick = { chartViewType = 0 },
                            label = { Text("Trend Kehadiran Pesakit") },
                            leadingIcon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chip_attendance_trend")
                        )

                        FilterChip(
                            selected = chartViewType == 1,
                            onClick = { chartViewType = 1 },
                            label = { Text("Metrik Pengeluaran Metadon") },
                            leadingIcon = { Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chip_dispensed_metrics")
                        )
                    }
                }
            }
        }

        // Summary KPI Metrics Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 1: Total Attendance
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Kehadiran (${activeMonthData.month})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${activeMonthData.totalAttendance} kali",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "DOT: ${activeMonthData.dotAttendance} | Bawa Balik: ${activeMonthData.takeHomeAttendance}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Card 2: Total Dispensed Volume & Dose
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ubat Dikeluarkan",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.1f", activeMonthData.totalVolumeLiters)} L",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${activeMonthData.totalDoseMg.toInt()} mg (${activeMonthData.totalVolumeMl.toInt()} mL)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 3: Attendance Compliance Rate
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StatusGreenContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Kadar Kepatuhan Rawatan",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${activeMonthData.complianceRate}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Standard KKM > 90.0%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF047857)
                            )
                        }
                    }

                    // Card 4: Average Dose / Patient
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Purata Dos Harian",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val avgDose = String.format(Locale.US, "%.1f", activeMonthData.totalDoseMg / activeMonthData.totalAttendance)
                            Text(
                                text = "$avgDose mg",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Kadar Pembekalan Teratur",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        // Primary Dynamic Visualization Canvas (Bar + Line Chart Component)
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_visualization_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (chartViewType == 0) "Graf Trend Kehadiran Pesakit MMT (2026)" else "Graf Pengeluaran Cecair Metadon (Liters & mg)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Sentuh bar bulan untuk melihat perincian khusus",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        // Category Filter Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selectedCategoryFilter == "SEMUA") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedCategoryFilter = "SEMUA" }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Semua",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCategoryFilter == "SEMUA") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selectedCategoryFilter == "DOT") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedCategoryFilter = "DOT" }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "DOT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCategoryFilter == "DOT") MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selectedCategoryFilter == "TAKE_HOME") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedCategoryFilter = "TAKE_HOME" }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Bawa Balik",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCategoryFilter == "TAKE_HOME") MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Compose Interactive Canvas Chart
                    MonthlyTrendsChartCanvas(
                        dataList = monthlyDataList,
                        selectedIndex = selectedMonthIndex,
                        chartViewType = chartViewType,
                        categoryFilter = selectedCategoryFilter,
                        onMonthSelected = { index -> selectedMonthIndex = index },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Chart Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (chartViewType == 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("DOT (Klinik)", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bawa Balik", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(StatusGreen))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Garis Trend", style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Isi Padu (Liters)", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Jumlah Dos (10k mg)", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // Active Month Inspection Breakdown Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Perincian Bulan: ${activeMonthData.month} ${activeMonthData.year}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Aktif Dilihat",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Transaksi Kehadiran", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("${activeMonthData.totalAttendance} kali", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pengeluaran Metadon (mL)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("${activeMonthData.totalVolumeMl.toInt()} mL", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Sub-kategori DOT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("${activeMonthData.dotAttendance} pesakit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sub-kategori Bawa Balik", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("${activeMonthData.takeHomeAttendance} pesakit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pengeluaran Metadon (L)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("${String.format(Locale.US, "%.2f", activeMonthData.totalVolumeLiters)} Liters", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyTrendsChartCanvas(
    dataList: List<MonthlyTrendData>,
    selectedIndex: Int,
    chartViewType: Int,
    categoryFilter: String,
    onMonthSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val trendLineColor = StatusGreen
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .pointerInput(dataList, chartViewType) {
                detectTapGestures { offset ->
                    val width = size.width
                    val sectionWidth = width / dataList.size
                    val clickedIndex = (offset.x / sectionWidth).toInt().coerceIn(0, dataList.size - 1)
                    onMonthSelected(clickedIndex)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val bottomPadding = 30.dp.toPx()
            val topPadding = 20.dp.toPx()
            val availableHeight = height - bottomPadding - topPadding

            val count = dataList.size
            val barWidth = (width / count) * 0.45f

            // Max value for scaling
            val maxAttendance = dataList.maxOfOrNull { it.totalAttendance }?.toFloat() ?: 700f
            val maxVolumeLiters = dataList.maxOfOrNull { it.totalVolumeLiters }?.toFloat() ?: 15f

            // Draw horizontal background grid lines
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val y = topPadding + (availableHeight / gridSteps) * i
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw Bar Charts & Trend Line Points
            val trendPoints = mutableListOf<Offset>()

            dataList.forEachIndexed { index, item ->
                val xCenter = (width / count) * index + (width / count) / 2f
                val isSelected = index == selectedIndex

                if (chartViewType == 0) {
                    // Attendance View
                    val valDot = when (categoryFilter) {
                        "TAKE_HOME" -> 0f
                        else -> item.dotAttendance.toFloat()
                    }
                    val valTakeHome = when (categoryFilter) {
                        "DOT" -> 0f
                        else -> item.takeHomeAttendance.toFloat()
                    }
                    val valTotal = valDot + valTakeHome

                    val heightDot = (valDot / maxAttendance) * availableHeight
                    val heightTakeHome = (valTakeHome / maxAttendance) * availableHeight
                    val heightTotal = (valTotal / maxAttendance) * availableHeight

                    val barLeft = xCenter - barWidth / 2f
                    val yBottom = height - bottomPadding

                    // Draw Stacked Bars
                    if (valTakeHome > 0) {
                        drawRoundRect(
                            color = if (isSelected) tertiaryColor else tertiaryColor.copy(alpha = 0.6f),
                            topLeft = Offset(barLeft, yBottom - heightTotal),
                            size = Size(barWidth, heightTakeHome + heightDot),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }

                    if (valDot > 0) {
                        drawRoundRect(
                            color = if (isSelected) primaryColor else primaryColor.copy(alpha = 0.7f),
                            topLeft = Offset(barLeft, yBottom - heightDot),
                            size = Size(barWidth, heightDot),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    val yPoint = yBottom - heightTotal
                    trendPoints.add(Offset(xCenter, yPoint))

                } else {
                    // Volume / Medication Dispensed View
                    val volumeL = item.totalVolumeLiters.toFloat()
                    val barHeight = (volumeL / maxVolumeLiters) * availableHeight
                    val barLeft = xCenter - barWidth / 2f
                    val yBottom = height - bottomPadding

                    drawRoundRect(
                        color = if (isSelected) secondaryColor else secondaryColor.copy(alpha = 0.7f),
                        topLeft = Offset(barLeft, yBottom - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    trendPoints.add(Offset(xCenter, yBottom - barHeight))
                }

                // Selection Highlight Line
                if (isSelected) {
                    drawLine(
                        color = primaryColor,
                        start = Offset(xCenter, topPadding),
                        end = Offset(xCenter, height - bottomPadding),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Month Text Labels
                val textPaint = android.graphics.Paint().apply {
                    color = if (isSelected) primaryColor.hashCode() else textColor.hashCode()
                    textSize = 12.sp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = isSelected
                }

                drawContext.canvas.nativeCanvas.drawText(
                    item.month,
                    xCenter,
                    height - 8.dp.toPx(),
                    textPaint
                )
            }

            // Draw Trend Curve Line
            if (trendPoints.size > 1) {
                val path = Path()
                trendPoints.forEachIndexed { idx, pt ->
                    if (idx == 0) path.moveTo(pt.x, pt.y)
                    else path.lineTo(pt.x, pt.y)
                }

                drawPath(
                    path = path,
                    color = trendLineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                trendPoints.forEach { pt ->
                    drawCircle(
                        color = trendLineColor,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = pt
                    )
                }
            }
        }
    }
}
