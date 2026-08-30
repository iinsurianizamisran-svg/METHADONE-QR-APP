package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.DispenseRecord
import com.example.ui.viewmodel.AttendanceSummary
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    /**
     * Generates CSV formatted text content with UTF-8 BOM for Microsoft Excel compatibility.
     */
    fun generateCsvContent(
        records: List<DispenseRecord>,
        summary: AttendanceSummary?,
        dateScope: String,
        officerName: String
    ): String {
        val sb = StringBuilder()
        // UTF-8 BOM for Excel
        sb.append("\uFEFF")

        // Metadata comments
        sb.appendLine("# LAPORAN AUDIT DISPENSI & KEHADIRAN METHADONE (MMT)")
        sb.appendLine("# Klinik Kesihatan Cheras - Program Rawatan Methadone KKM")
        sb.appendLine("# Skop Tarikh: $dateScope")
        sb.appendLine("# Pegawai Bertanggungjawab: $officerName")
        sb.appendLine("# Tarikh & Masa Dijana: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")

        if (summary != null) {
            sb.appendLine("# Ringkasan Audit: Total Registered=${summary.totalRegistered}, Attended=${summary.attendedCount}, Pending=${summary.pendingCount}, Total Mg=${summary.totalMgDispensed}mg, Total Ml=${summary.totalMlDispensed}mL, DOT=${summary.dotCount}, TakeHome=${summary.takeHomeCount}")
        }
        sb.appendLine("#")

        // CSV Header
        sb.appendLine("No,Tarikh,Masa,ID Pesakit,Nama Pesakit,No. K/P,Dos (mg),Isipadu (mL),Jenis Dispensi,Botol Bawa Balik,Botol Dipulangkan,Petugas Farmasi,Kaedah Imbasan,Status Kehadiran,Catatan")

        // CSV Data Rows
        records.forEachIndexed { index, rec ->
            val row = listOf(
                (index + 1).toString(),
                escapeCsv(rec.dispenseDate),
                escapeCsv(rec.dispenseTime),
                escapeCsv(rec.patientId),
                escapeCsv(rec.patientName),
                escapeCsv(rec.patientIc),
                rec.doseMg.toString(),
                rec.doseVolumeMl.toString(),
                escapeCsv(rec.dispenseType),
                rec.takeHomeBottlesCount.toString(),
                if (rec.bottlesReturned) "Ya" else "Tidak",
                escapeCsv(rec.officerName),
                escapeCsv(rec.scanMethod),
                escapeCsv(rec.attendanceStatus),
                escapeCsv(rec.remarks)
            )
            sb.appendLine(row.joinToString(","))
        }

        return sb.toString()
    }

    private fun escapeCsv(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }

    /**
     * Saves CSV content into a temporary cache file and returns the File object.
     */
    fun exportToCsvFile(
        context: Context,
        records: List<DispenseRecord>,
        summary: AttendanceSummary?,
        dateScope: String,
        officerName: String
    ): File {
        val fileName = "Audit_Methadone_${dateScope.replace(" ", "_").replace("/", "-")}_${System.currentTimeMillis()}.csv"
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)

        val csvContent = generateCsvContent(records, summary, dateScope, officerName)
        FileOutputStream(file).use { out ->
            out.write(csvContent.toByteArray(Charsets.UTF_8))
        }

        return file
    }

    /**
     * Generates a PDF audit document using Android Native PdfDocument API.
     */
    fun exportToPdfFile(
        context: Context,
        records: List<DispenseRecord>,
        summary: AttendanceSummary?,
        dateScope: String,
        officerName: String
    ): File {
        val pdfDocument = PdfDocument()

        // Page Specs: A4 Size in points (595 x 842 pt)
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 14f
            color = Color.WHITE
        }
        val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 9f
            color = Color.LTGRAY
        }
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 9f
            color = Color.rgb(30, 41, 59)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 8f
            color = Color.rgb(51, 65, 85)
        }
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 8.5f
            color = Color.WHITE
        }

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        fun drawPageHeader(canvas: Canvas, pageNum: Int) {
            // Top Primary Header Banner
            val bannerPaint = Paint().apply { color = Color.rgb(15, 118, 110) } // Teal Primary
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 60f, bannerPaint)

            canvas.drawText("KLINIK KESIHATAN CHERAS - KEMENTERIAN KESIHATAN MALAYSIA", 20f, 24f, titlePaint)
            canvas.drawText("LAPORAN AUDIT PENDISPENSIAN & KEHADIRAN PROGRAM METHADONE (MMT)", 20f, 42f, subTitlePaint)

            // Sub Header info bar
            val infoBgPaint = Paint().apply { color = Color.rgb(241, 245, 249) }
            canvas.drawRect(0f, 60f, pageWidth.toFloat(), 95f, infoBgPaint)

            canvas.drawText("Skop Audit: $dateScope", 20f, 78f, headerTextPaint)
            canvas.drawText("Petugas Farmasi: $officerName", 220f, 78f, headerTextPaint)
            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Muka Surat: $pageNum | Dijana: $timeStr", 400f, 78f, bodyPaint)
        }

        drawPageHeader(canvas, pageNumber)

        var currentY = 110f

        // Draw Summary Box if available and on Page 1
        if (summary != null) {
            val boxPaint = Paint().apply {
                color = Color.rgb(240, 253, 250) // Teal tint
                style = Paint.Style.FILL
            }
            val borderPaint = Paint().apply {
                color = Color.rgb(13, 148, 136)
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            canvas.drawRoundRect(RectF(20f, currentY, pageWidth - 20f, currentY + 55f), 8f, 8f, boxPaint)
            canvas.drawRoundRect(RectF(20f, currentY, pageWidth - 20f, currentY + 55f), 8f, 8f, borderPaint)

            val statTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 9f
                color = Color.rgb(15, 118, 110)
            }
            canvas.drawText("RINGKASAN AUDIT KEHADIRAN & STOK METHADONE", 30f, currentY + 16f, statTitlePaint)

            val statText = "Total Berdaftar: ${summary.totalRegistered} org | Hadir: ${summary.attendedCount} org | Belum Hadir: ${summary.pendingCount} org"
            val statText2 = "Jumlah Cecair Dikeluarkan: ${summary.totalMgDispensed.toInt()} mg (${String.format(Locale.US, "%.1f", summary.totalMlDispensed)} mL) | DOT: ${summary.dotCount} org | Bawa Balik: ${summary.takeHomeCount} org"

            canvas.drawText(statText, 30f, currentY + 32f, bodyPaint)
            canvas.drawText(statText2, 30f, currentY + 46f, bodyPaint)

            currentY += 70f
        }

        // Draw Table Header
        fun drawTableHeader(canvas: Canvas, y: Float) {
            val thBgPaint = Paint().apply { color = Color.rgb(30, 41, 59) } // Slate dark
            canvas.drawRect(20f, y, pageWidth - 20f, y + 22f, thBgPaint)

            val colY = y + 15f
            canvas.drawText("#", 25f, colY, tableHeaderPaint)
            canvas.drawText("Masa", 45f, colY, tableHeaderPaint)
            canvas.drawText("ID Pesakit", 95f, colY, tableHeaderPaint)
            canvas.drawText("Nama Pesakit", 175f, colY, tableHeaderPaint)
            canvas.drawText("No. K/P", 330f, colY, tableHeaderPaint)
            canvas.drawText("Dos (mg)", 415f, colY, tableHeaderPaint)
            canvas.drawText("Jenis", 475f, colY, tableHeaderPaint)
            canvas.drawText("Petugas", 525f, colY, tableHeaderPaint)
        }

        drawTableHeader(canvas, currentY)
        currentY += 22f

        val rowBgPaintEven = Paint().apply { color = Color.WHITE }
        val rowBgPaintOdd = Paint().apply { color = Color.rgb(248, 250, 252) }
        val gridLinePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.5f
        }

        // Draw Table Rows
        records.forEachIndexed { index, rec ->
            if (currentY + 22f > pageHeight - 70f) {
                // End current page & start new page
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas

                drawPageHeader(canvas, pageNumber)
                currentY = 110f
                drawTableHeader(canvas, currentY)
                currentY += 22f
            }

            val bgPaint = if (index % 2 == 0) rowBgPaintEven else rowBgPaintOdd
            canvas.drawRect(20f, currentY, pageWidth - 20f, currentY + 20f, bgPaint)
            canvas.drawLine(20f, currentY + 20f, pageWidth - 20f, currentY + 20f, gridLinePaint)

            val rowY = currentY + 14f
            canvas.drawText("${index + 1}", 25f, rowY, bodyPaint)
            canvas.drawText(rec.dispenseTime, 45f, rowY, bodyPaint)
            canvas.drawText(rec.patientId, 95f, rowY, bodyPaint)

            val truncatedName = if (rec.patientName.length > 22) rec.patientName.take(20) + ".." else rec.patientName
            canvas.drawText(truncatedName, 175f, rowY, bodyPaint)
            canvas.drawText(rec.patientIc, 330f, rowY, bodyPaint)
            canvas.drawText("${rec.doseMg.toInt()} mg", 415f, rowY, headerTextPaint)
            canvas.drawText(rec.dispenseType, 475f, rowY, bodyPaint)

            val truncatedOfficer = if (rec.officerName.length > 10) rec.officerName.take(8) + ".." else rec.officerName
            canvas.drawText(truncatedOfficer, 525f, rowY, bodyPaint)

            currentY += 20f
        }

        // Verification & Sign-off Stamp Box at bottom of last page
        if (currentY + 60f < pageHeight - 30f) {
            currentY += 15f
            val stampBorderPaint = Paint().apply {
                color = Color.rgb(148, 163, 184)
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
            }
            canvas.drawRect(340f, currentY, pageWidth - 20f, currentY + 50f, stampBorderPaint)
            canvas.drawText("PENGESAHAN AUDIT KLINIK", 350f, currentY + 14f, headerTextPaint)
            canvas.drawText("Tandatangan Pegawai Farmasi Y/M:", 350f, currentY + 28f, bodyPaint)
            canvas.drawText("Tarikh: ____________________", 350f, currentY + 42f, bodyPaint)
        }

        pdfDocument.finishPage(page)

        val fileName = "Laporan_Audit_Methadone_${dateScope.replace(" ", "_").replace("/", "-")}_${System.currentTimeMillis()}.pdf"
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    /**
     * Shares a file via Android Intent System Chooser.
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title\nFail dijana secara automatik oleh Sistem e-Methadone QR.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Kongsi / Simpan $title")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Copies text content to device clipboard.
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Laporan Methadone") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label disalin ke papan keratan (Clipboard)!", Toast.LENGTH_SHORT).show()
    }
}
