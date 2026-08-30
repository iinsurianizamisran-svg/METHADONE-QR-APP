package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.model.InventoryItem
import com.example.data.model.InventoryLog
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusAmberContainer
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenContainer
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedContainer
import com.example.ui.viewmodel.MethadoneViewModel
import java.util.Locale

@Composable
fun InventoryScreen(
    viewModel: MethadoneViewModel,
    modifier: Modifier = Modifier
) {
    val inventoryItem by viewModel.inventoryItem.collectAsStateWithLifecycle()
    val inventoryLogs by viewModel.inventoryLogs.collectAsStateWithLifecycle()
    val isLowStock by viewModel.isLowStockAlert.collectAsStateWithLifecycle()

    var showRestockDialog by remember { mutableStateOf(false) }
    var showThresholdDialog by remember { mutableStateOf(false) }
    var showAdjustmentDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Bar Surface
        item {
            Surface(
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pengurusan Inventori Metadon",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pemantauan Bekalan & Amaran Stok Rendah",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(
                        onClick = { showThresholdDialog = true },
                        modifier = Modifier.testTag("configure_threshold_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Tetapkan Had Amaran",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Low Stock Alert Banner (Triggered automatically when stock drops below threshold)
        if (isLowStock) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusRedContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("low_stock_alert_banner")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(StatusRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AMARAN STOK METADON RENDAH!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StatusRed
                                )
                                Text(
                                    text = "Paras stok baki di bawah Had Pesanan Semula (${inventoryItem.reorderThresholdMl.toInt()} mL).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF991B1B),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Sila buat pesanan bekalan baharu kepada Stor Logistik Farmasi KKM bagi memastikan tiada gangguan dispensasi rawatan harian pesakit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7F1D1D)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { showRestockDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("alert_restock_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Terima Bekalan (Restock)")
                            }

                            OutlinedButton(
                                onClick = { showThresholdDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                                modifier = Modifier.testTag("alert_threshold_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Ubah Had")
                            }
                        }
                    }
                }
            }
        }

        // Current Stock Status Overview Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (isLowStock) StatusRedContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = inventoryItem.medicationName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isLowStock) StatusRed else StatusGreen
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isLowStock) "STOK RENDAH" else "STOK MENCUKUPI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Jumlah Stok Baki Cecair:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${inventoryItem.currentStockMl.toInt()}",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = " mL",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Nisbah Liter:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.2f", inventoryItem.currentStockLiters)} Liter",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress indicator
                    val progress = (inventoryItem.currentStockMl / (inventoryItem.lastRestockMl.coerceAtLeast(inventoryItem.reorderThresholdMl * 2.0))).coerceIn(0.0, 1.0).toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (isLowStock) StatusRed else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Anggaran Botol 500mL: ~${(inventoryItem.currentStockMl / 500.0).toInt()} botol",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Had Amaran: ${inventoryItem.reorderThresholdMl.toInt()} mL",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) StatusRed else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "No. Batch: ${inventoryItem.batchNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Tamat Tempoh: ${inventoryItem.expiryDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Action Control Buttons
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { showRestockDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("restock_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Terima Bekalan", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showThresholdDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("configure_threshold_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Had Pesanan", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showAdjustmentDialog = true },
                    modifier = Modifier.testTag("adjust_stock_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Pelarasan Manual")
                }
            }
        }

        // Section Header: Transaction Audit Logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Log Pergerakan & Audit Inventori (${inventoryLogs.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Kemaskini Masa Nyata",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Inventory Logs List
        if (inventoryLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tiada log transaksi inventori direkodkan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(inventoryLogs, key = { it.id }) { log ->
                InventoryLogItemCard(log = log, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    // Dialog: Configure Reorder Threshold
    if (showThresholdDialog) {
        ConfigureThresholdDialog(
            currentThresholdMl = inventoryItem.reorderThresholdMl,
            onDismiss = { showThresholdDialog = false },
            onSave = { newThresholdMl ->
                viewModel.updateReorderThreshold(newThresholdMl)
                showThresholdDialog = false
            }
        )
    }

    // Dialog: Restock Inventory
    if (showRestockDialog) {
        RestockInventoryDialog(
            currentBatch = inventoryItem.batchNumber,
            currentSupplier = inventoryItem.supplierName,
            onDismiss = { showRestockDialog = false },
            onRestock = { addMl, batch, expiry, supplier, notes ->
                viewModel.restockInventory(addMl, batch, expiry, supplier, notes)
                showRestockDialog = false
            }
        )
    }

    // Dialog: Manual Stock Adjustment
    if (showAdjustmentDialog) {
        AdjustStockDialog(
            currentStockMl = inventoryItem.currentStockMl,
            onDismiss = { showAdjustmentDialog = false },
            onAdjust = { newStockMl, reason ->
                viewModel.adjustStockLevel(newStockMl, reason)
                showAdjustmentDialog = false
            }
        )
    }
}

@Composable
fun InventoryLogItemCard(
    log: InventoryLog,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.testTag("inventory_log_item_${log.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, tintColor, bgColor) = when (log.actionType) {
                "TAMBAH_STOK" -> Triple(Icons.Default.ArrowDownward, StatusGreen, StatusGreenContainer)
                "DISPENSI_AUTOMATIK" -> Triple(Icons.Default.WaterDrop, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                "KEMASKINI_HAD" -> Triple(Icons.Default.Tune, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer)
                else -> Triple(Icons.Default.Edit, StatusAmber, StatusAmberContainer)
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tarikh: ${log.date} ${log.time} • Oleh: ${log.officerName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (log.volumeChangeMl != 0.0)
                            "Perubahan: ${if (log.volumeChangeMl > 0) "+${log.volumeChangeMl.toInt()}" else "${log.volumeChangeMl.toInt()}"} mL"
                        else
                            "Tindakan Pentadbir",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (log.volumeChangeMl > 0) StatusGreen else if (log.volumeChangeMl < 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Baki Stok: ${log.remainingStockMl.toInt()} mL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun ConfigureThresholdDialog(
    currentThresholdMl: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var thresholdMl by remember { mutableDoubleStateOf(currentThresholdMl) }
    var customInput by remember { mutableStateOf("") }

    val presetOptions = listOf(500.0, 1000.0, 1500.0, 2000.0, 3000.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tetapkan Had Amaran Stok (Reorder Threshold)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Apabila stok cecair metadon berada di bawah paras had ini, sistem akan menjana amaran automatik untuk pesanan semula:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "HAD AMARAN SEMASA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${thresholdMl.toInt()} mL (${String.format(Locale.US, "%.1f", thresholdMl / 1000.0)} Liter)",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "Pilihan Pantas Paras Had (mL):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetOptions.forEach { option ->
                        val isSelected = thresholdMl == option
                        OutlinedButton(
                            onClick = {
                                thresholdMl = option
                                customInput = ""
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${option.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Slider(
                    value = thresholdMl.toFloat(),
                    onValueChange = { thresholdMl = it.toDouble() },
                    valueRange = 250f..5000f,
                    steps = 18,
                    modifier = Modifier.testTag("threshold_slider")
                )

                OutlinedTextField(
                    value = customInput,
                    onValueChange = { input ->
                        customInput = input
                        val parsed = input.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            thresholdMl = parsed
                        }
                    },
                    label = { Text("Atau Masukkan Had Custom (mL)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_threshold_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(thresholdMl) },
                modifier = Modifier.testTag("save_threshold_button")
            ) {
                Text("Simpan Had Amaran")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun RestockInventoryDialog(
    currentBatch: String,
    currentSupplier: String,
    onDismiss: () -> Unit,
    onRestock: (addMl: Double, batch: String, expiry: String, supplier: String, notes: String) -> Unit
) {
    var volumeMlText by remember { mutableStateOf("5000") }
    var batchNumber by remember { mutableStateOf(currentBatch) }
    var expiryDate by remember { mutableStateOf("2027-12-31") }
    var supplierName by remember { mutableStateOf(currentSupplier) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Borang Penerimaan Stok Metadon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Rekodkan penerimaan stok cecair metadon baharu daripada pembekal / KKM Store:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                OutlinedTextField(
                    value = volumeMlText,
                    onValueChange = { volumeMlText = it },
                    label = { Text("Isi Padu Diterima (mL)*") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restock_volume_input")
                )

                OutlinedTextField(
                    value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = { Text("Nombor Batch Lot*") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restock_batch_input")
                )

                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Tarikh Luput (YYYY-MM-DD)*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text("Pembekal / Sumber Stok") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Penerimaan (Pilihan)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val volume = volumeMlText.toDoubleOrNull() ?: 0.0
                    if (volume > 0) {
                        onRestock(volume, batchNumber, expiryDate, supplierName, notes)
                    }
                },
                modifier = Modifier.testTag("submit_restock_button")
            ) {
                Text("Tambah Stok")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun AdjustStockDialog(
    currentStockMl: Double,
    onDismiss: () -> Unit,
    onAdjust: (newStockMl: Double, reason: String) -> Unit
) {
    var newStockText by remember { mutableStateOf(currentStockMl.toInt().toString()) }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = StatusAmber)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pelarasan Stok Manual",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Gunakan borang ini jika terdapat perbezaan antara baki fizikal dan baki dalam sistem (cth: tumpah, ujian kualiti):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                OutlinedTextField(
                    value = newStockText,
                    onValueChange = { newStockText = it },
                    label = { Text("Baki Stok Fizikal Baharu (mL)*") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adjust_stock_input")
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Sebab Pelarasan (Wajib)*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adjust_reason_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newStock = newStockText.toDoubleOrNull()
                    if (newStock != null && newStock >= 0 && reason.isNotBlank()) {
                        onAdjust(newStock, reason.trim())
                    }
                },
                modifier = Modifier.testTag("submit_adjust_button")
            ) {
                Text("Sahkan Pelarasan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
