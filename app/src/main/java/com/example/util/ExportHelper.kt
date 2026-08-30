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
import com.example.data.model.ClinicSettings
import com.example.data.model.DispenseRecord
import com.example.data.model.Patient
import com.example.ui.viewmodel.AttendanceSummary
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    /**
     * Generates NDMA Monthly Report in CSV format.
     */
    fun generateNkmaReportCsv(
        settings: ClinicSettings,
        monthYear: String,
        dispenseRecords: List<DispenseRecord>,
        patientList: List<Patient>
    ): String {
        val sb = StringBuilder()
        sb.append("\uFEFF") // UTF-8 BOM

        sb.appendLine("# LAPORAN BULANAN NDMA (National Drugs Malaysia Association) - PROGRAM RAWATAN METHADONE (MMT)")
        sb.appendLine("# Nama Klinik / PKD: ${settings.clinicName}")
        sb.appendLine("# Bulan Laporan: $monthYear")
        sb.appendLine("# Tarikh Dijana: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
        sb.appendLine("#")

        sb.appendLine("Kategori METRIK NDMA,Jumlah (Pesakit)")
        sb.appendLine("Klinik / PKD,${escapeCsv(settings.clinicName)}")
        sb.appendLine("Pesakit Aktif Semasa (Current Total Active),${settings.activePatientsCount}")
        sb.appendLine("Kes Baharu Didaftar (New Case Register),${settings.newCasesCount}")
        sb.appendLine("Cicir Rawatan (Defaulter),${settings.defaultersCount}")
        sb.appendLine("Sambung Rawatan (Restart),${settings.restartCount}")
        sb.appendLine("Pindah Masuk (Transfer-In),${settings.transferInCount}")
        sb.appendLine("Pindah Keluar (Transfer-Out),${settings.transferOutCount}")
        sb.appendLine("Kematian (Death),${settings.deathCount}")
        sb.appendLine("Tamat Rawatan (Terminated),${settings.terminatedCount}")

        val netActive = (settings.activePatientsCount + settings.newCasesCount + settings.transferInCount + settings.restartCount) -
                (settings.defaultersCount + settings.transferOutCount + settings.deathCount + settings.terminatedCount)
        sb.appendLine("ANGGARAN PESAKIT AKTIF AKHIR BULAN,${netActive}")
        sb.appendLine("#")

        sb.appendLine("REKOD DISPENSI METHADONE BULANAN")
        sb.appendLine("No,ID Pesakit,Nama Pesakit,No. K/P,Tarikh,Masa,Dos (mg),Isipadu (mL),Jenis,Petugas")
        dispenseRecords.forEachIndexed { idx, r ->
            val row = listOf(
                (idx + 1).toString(),
                escapeCsv(r.patientId),
                escapeCsv(r.patientName),
                escapeCsv(r.patientIc),
                escapeCsv(r.dispenseDate),
                escapeCsv(r.dispenseTime),
                r.doseMg.toString(),
                r.doseVolumeMl.toString(),
                escapeCsv(r.dispenseType),
                escapeCsv(r.officerName)
            )
            sb.appendLine(row.joinToString(","))
        }

        return sb.toString()
    }

    /**
     * Generates NDMA Monthly PDF Document.
     */
    fun generateNkmaReportPdf(
        context: Context,
        settings: ClinicSettings,
        monthYear: String,
        dispenseRecords: List<DispenseRecord>
    ): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 width in points
        val pageHeight = 842 // A4 height

        val pageNumber = 1
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val sectionPaint = Paint().apply {
            color = Color.rgb(30, 58, 138)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerTextPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val bgPaint = Paint().apply { color = Color.rgb(241, 245, 249) }
        canvas.drawRect(20f, 20f, pageWidth - 20f, 85f, bgPaint)

        canvas.drawText("KEMENTERIAN KESIHATAN MALAYSIA (KKM)", 30f, 40f, titlePaint)
        canvas.drawText("LAPORAN BULANAN NDMA (National Drugs Malaysia Association)", 30f, 56f, sectionPaint)
        canvas.drawText("Klinik / PKD: ${settings.clinicName} | Laporan: $monthYear", 30f, 72f, subtitlePaint)

        var currentY = 105f
        canvas.drawText("RINGKASAN METRIK BULANAN NDMA", 20f, currentY, sectionPaint)
        currentY += 15f

        val nkmaMetrics = listOf(
            "1. Current Total Active Patient (Pesakit Aktif Semasa)" to settings.activePatientsCount.toString(),
            "2. New Case Register (Kes Baharu Didaftar)" to settings.newCasesCount.toString(),
            "3. Defaulter (Cicir Rawatan)" to settings.defaultersCount.toString(),
            "4. Restart (Sambung Rawatan Semula)" to settings.restartCount.toString(),
            "5. Transfer-In (Pindah Masuk)" to settings.transferInCount.toString(),
            "6. Transfer-Out (Pindah Keluar)" to settings.transferOutCount.toString(),
            "7. Death (Kematian)" to settings.deathCount.toString(),
            "8. Terminated (Tamat Rawatan)" to settings.terminatedCount.toString()
        )

        val netActive = (settings.activePatientsCount + settings.newCasesCount + settings.transferInCount + settings.restartCount) -
                (settings.defaultersCount + settings.transferOutCount + settings.deathCount + settings.terminatedCount)

        val gridPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.5f
        }

        nkmaMetrics.forEachIndexed { i, (label, valStr) ->
            val bg = if (i % 2 == 0) Color.rgb(248, 250, 252) else Color.WHITE
            val p = Paint().apply { color = bg }
            canvas.drawRect(20f, currentY, pageWidth - 20f, currentY + 18f, p)
            canvas.drawLine(20f, currentY + 18f, pageWidth - 20f, currentY + 18f, gridPaint)

            canvas.drawText(label, 30f, currentY + 13f, bodyPaint)
            canvas.drawText(valStr, 480f, currentY + 13f, headerTextPaint)
            currentY += 18f
        }

        // Net Total Row
        val totalBg = Paint().apply { color = Color.rgb(224, 231, 255) }
        canvas.drawRect(20f, currentY, pageWidth - 20f, currentY + 22f, totalBg)
        canvas.drawText("JUMLAH PESAKIT AKTIF AKHIR BULAN (NET ACTIVE)", 30f, currentY + 15f, headerTextPaint)
        canvas.drawText("$netActive Orang", 480f, currentY + 15f, headerTextPaint)
        currentY += 35f

        val stampBorderPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        canvas.drawRect(340f, currentY, pageWidth - 20f, currentY + 65f, stampBorderPaint)
        canvas.drawText("PENGESAHAN PEGAWAI FARMASI / KETAKA", 350f, currentY + 16f, headerTextPaint)
        canvas.drawText("Nama Pegawai: ___________________________", 350f, currentY + 34f, bodyPaint)
        canvas.drawText("Tandatangan & Cop: ______________________", 350f, currentY + 50f, bodyPaint)

        pdfDocument.finishPage(page)

        val fileName = "Laporan_NKMA_${settings.clinicName.replace(" ", "_")}_${monthYear.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

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
