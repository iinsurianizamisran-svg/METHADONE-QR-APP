package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DispenseRecord
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenContainer
import com.example.ui.viewmodel.AttendanceSummary
import com.example.ui.viewmodel.MethadoneViewModel
import com.example.util.ExportHelper

enum class ExportFormat {
    CSV,
    PDF,
    TEXT_COPY
}

enum class ExportScope {
    SELECTED_DATE,
    ALL_HISTORICAL
}

@Composable
fun ExportModal(
    selectedDate: String,
    dateRecords: List<DispenseRecord>,
    allRecords: List<DispenseRecord>,
    summary: AttendanceSummary,
    officerName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var exportFormat by remember { mutableStateOf(ExportFormat.PDF) }
    var exportScope by remember { mutableStateOf(ExportScope.SELECTED_DATE) }

    val activeRecords = if (exportScope == ExportScope.SELECTED_DATE) dateRecords else allRecords
    val scopeText = if (exportScope == ExportScope.SELECTED_DATE) "Tarikh: $selectedDate" else "Semua Rekod Auditing (${allRecords.size} Transaksi)"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Modal Title Header
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
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Eksport Laporan Audit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Format CSV & PDF Audit KKM",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_export_modal_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Choose Scope
                Text(
                    text = "1. Pilih Skop Data Audit",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScopeSelectionChip(
                        title = "Tarikh Hari Ini ($selectedDate)",
                        countText = "${dateRecords.size} rekod",
                        isSelected = exportScope == ExportScope.SELECTED_DATE,
                        onClick = { exportScope = ExportScope.SELECTED_DATE },
                        modifier = Modifier.weight(1f).testTag("scope_today_chip")
                    )

                    ScopeSelectionChip(
                        title = "Semua Rekod Sejarah",
                        countText = "${allRecords.size} rekod",
                        isSelected = exportScope == ExportScope.ALL_HISTORICAL,
                        onClick = { exportScope = ExportScope.ALL_HISTORICAL },
                        modifier = Modifier.weight(1f).testTag("scope_all_chip")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Choose Format
                Text(
                    text = "2. Pilih Format Fail Eksport",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormatOptionCard(
                        icon = Icons.Default.PictureAsPdf,
                        iconColor = Color(0xFFDC2626),
                        title = "Dokumen PDF Auditing (.pdf)",
                        subtitle = "Sesuai untuk cetakan rasmi, semakan FMS & tandatangan audit",
                        isSelected = exportFormat == ExportFormat.PDF,
                        onClick = { exportFormat = ExportFormat.PDF },
                        testTag = "format_pdf_option"
                    )

                    FormatOptionCard(
                        icon = Icons.Default.TableChart,
                        iconColor = Color(0xFF16A34A),
                        title = "Jadual CSV / Excel (.csv)",
                        subtitle = "Fail data mentah sesuai untuk Microsoft Excel & Google Sheets",
                        isSelected = exportFormat == ExportFormat.CSV,
                        onClick = { exportFormat = ExportFormat.CSV },
                        testTag = "format_csv_option"
                    )

                    FormatOptionCard(
                        icon = Icons.Default.ContentCopy,
                        iconColor = Color(0xFF2563EB),
                        title = "Salin Teks (Clipboard)",
                        subtitle = "Salin format ringkasan pantas untuk perkongsian mesej/WhatsApp",
                        isSelected = exportFormat == ExportFormat.TEXT_COPY,
                        onClick = { exportFormat = ExportFormat.TEXT_COPY },
                        testTag = "format_copy_option"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Preview Info Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Jumlah Transaksi Akan Dieksport: ${activeRecords.size} rekod",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pegawai Auditing: $officerName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_export_button")
                    ) {
                        Text("Batal")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (activeRecords.isEmpty()) {
                                Toast.makeText(context, "Tiada rekod untuk dieksport pada skop ini.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            when (exportFormat) {
                                ExportFormat.CSV -> {
                                    val csvFile = ExportHelper.exportToCsvFile(
                                        context = context,
                                        records = activeRecords,
                                        summary = if (exportScope == ExportScope.SELECTED_DATE) summary else null,
                                        dateScope = scopeText,
                                        officerName = officerName
                                    )
                                    ExportHelper.shareFile(context, csvFile, "text/csv", "Laporan Audit Methadone (CSV)")
                                    Toast.makeText(context, "Fail CSV sedia dieksport!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }

                                ExportFormat.PDF -> {
                                    val pdfFile = ExportHelper.exportToPdfFile(
                                        context = context,
                                        records = activeRecords,
                                        summary = if (exportScope == ExportScope.SELECTED_DATE) summary else null,
                                        dateScope = scopeText,
                                        officerName = officerName
                                    )
                                    ExportHelper.shareFile(context, pdfFile, "application/pdf", "Laporan Audit Methadone (PDF)")
                                    Toast.makeText(context, "Dokumen PDF sedia dieksport!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }

                                ExportFormat.TEXT_COPY -> {
                                    val csvContent = ExportHelper.generateCsvContent(
                                        records = activeRecords,
                                        summary = if (exportScope == ExportScope.SELECTED_DATE) summary else null,
                                        dateScope = scopeText,
                                        officerName = officerName
                                    )
                                    ExportHelper.copyToClipboard(context, csvContent, "Data CSV Audit Methadone")
                                    onDismiss()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("confirm_export_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (exportFormat == ExportFormat.TEXT_COPY) "Salin Teks" else "Eksport & Kongsi",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScopeSelectionChip(
    title: String,
    countText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = countText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun FormatOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
