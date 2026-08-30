package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object QrCodeUtil {

    /**
     * Generates a high-quality QR code bitmap from raw text.
     */
    fun generateQrBitmap(
        content: String,
        sizePx: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1)
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            }

            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                sizePx,
                sizePx,
                hints
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) foregroundColor else backgroundColor
                }
            }

            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Parses scanned text.
     * Supports formats:
     * - "METH_QR|PATIENT_ID|IC|NAME|DOSE|DISPENSE_TYPE"
     * - "METH-2026-0012" (Direct ID)
     * - Plain IC format "840512-10-5543" or "840512105543"
     */
    fun extractPatientIdentifier(rawScanData: String): String {
        val trimmed = rawScanData.trim()
        if (trimmed.startsWith("METH_QR|")) {
            val parts = trimmed.split("|")
            if (parts.size >= 2) {
                return parts[1].trim()
            }
        }
        return trimmed
    }

    /**
     * Generates a beautiful, high-resolution MOH-themed digital ID card bitmap (CR80 Credit Card ratio: 1012 x 638 pixels).
     */
    fun generateDigitalIdCardBitmap(
        context: android.content.Context,
        patient: com.example.data.model.Patient,
        qrSize: Int = 180
    ): Bitmap? {
        val width = 1012
        val height = 638
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // Light background
        val bgPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#F8FAFC") // slate 50
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Dark navy slate card header
        val headerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A") // slate 900
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), 115f, headerPaint)

        // Top Header text
        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 23f
            isAntiAlias = true
            typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
        }
        canvas.drawText("KEMENTERIAN KESIHATAN MALAYSIA", 40f, 48f, titlePaint)

        val subTitlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#38BDF8") // sky 400
            textSize = 17f
            isAntiAlias = true
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
        }
        canvas.drawText("PROGRAM TERAPI GANTIAN METADON (MMT)", 40f, 82f, subTitlePaint)

        // KKM Badge block on right
        val badgePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#1E293B")
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawRect(780f, 22f, 970f, 92f, badgePaint)

        val badgeTextPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 16f
            isAntiAlias = true
            typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
        }
        canvas.drawText("KAD KKM MMT", 805f, 62f, badgeTextPaint)

        // Patient Photo Frame on left
        val photoBorderPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8") // slate 400
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3f
        }
        val photoBgPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#E2E8F0") // slate 200
            style = android.graphics.Paint.Style.FILL
        }
        val photoLeft = 50f
        val photoTop = 155f
        val photoWidth = 190f
        val photoHeight = 250f
        canvas.drawRect(photoLeft, photoTop, photoLeft + photoWidth, photoTop + photoHeight, photoBgPaint)
        canvas.drawRect(photoLeft, photoTop, photoLeft + photoWidth, photoTop + photoHeight, photoBorderPaint)

        // Draw standard portrait avatar inside photo frame
        val avatarPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#64748B") // slate 500
            style = android.graphics.Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(photoLeft + 95f, photoTop + 85f, 32f, avatarPaint)
        val path = android.graphics.Path()
        path.moveTo(photoLeft + 30f, photoTop + 230f)
        path.quadTo(photoLeft + 95f, photoTop + 145f, photoLeft + 160f, photoTop + 230f)
        canvas.drawPath(path, avatarPaint)

        val photoLabelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#475569")
            textSize = 13f
            isAntiAlias = true
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
        }
        canvas.drawText("FOTO PESAKIT", photoLeft + 42f, photoTop + 240f, photoLabelPaint)

        // Middle Patient details section
        val detailLabelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#475569") // slate 600
            textSize = 15f
            isAntiAlias = true
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
        }
        val detailValPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A") // slate 900
            textSize = 19f
            isAntiAlias = true
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
        }

        val startX = 270f
        var currentY = 185f

        canvas.drawText("NAMA PESAKIT:", startX, currentY, detailLabelPaint)
        currentY += 26f
        val maxLen = 32
        val truncatedName = if (patient.name.length > maxLen) patient.name.substring(0, maxLen - 3) + "..." else patient.name
        canvas.drawText(truncatedName.uppercase(), startX, currentY, detailValPaint)

        currentY += 40f
        canvas.drawText("KAD PENDAFTARAN MMT:", startX, currentY, detailLabelPaint)
        currentY += 26f
        canvas.drawText(patient.patientId, startX, currentY, detailValPaint)

        currentY += 40f
        canvas.drawText("NO. KAD PENGENALAN (IC):", startX, currentY, detailLabelPaint)
        currentY += 26f
        canvas.drawText(patient.icNumber, startX, currentY, detailValPaint)

        currentY += 40f
        canvas.drawText("DOS PRESKRIPSI HARIAN:", startX, currentY, detailLabelPaint)
        currentY += 26f
        canvas.drawText("${patient.currentDoseMg.toInt()} mg (${patient.doseVolumeMl} mL)", startX, currentY, detailValPaint)

        // QR Code Container on far right
        val qrPayload = patient.toQrPayload()
        val qrBitmap = generateQrBitmap(qrPayload, qrSize, android.graphics.Color.BLACK, android.graphics.Color.WHITE)
        if (qrBitmap != null) {
            val qrLeft = 750f
            val qrTop = 155f
            
            val qrCardPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                style = android.graphics.Paint.Style.FILL
            }
            val qrCardBorder = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#94A3B8")
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRect(qrLeft - 8f, qrTop - 8f, qrLeft + qrSize + 8f, qrTop + qrSize + 8f, qrCardPaint)
            canvas.drawRect(qrLeft - 8f, qrTop - 8f, qrLeft + qrSize + 8f, qrTop + qrSize + 8f, qrCardBorder)
            canvas.drawBitmap(qrBitmap, qrLeft, qrTop, null)

            // Scannable caption
            val qrCaptionPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#0F172A")
                textSize = 13f
                isAntiAlias = true
                typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
            }
            canvas.drawText("IMBAS KOD QR DI SINI", qrLeft + 8f, qrTop + qrSize + 28f, qrCaptionPaint)
        }

        // Green footer bar (clinical authentication)
        val footerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#10B981") // emerald 500
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawRect(0f, 580f, width.toFloat(), height.toFloat(), footerPaint)

        val footerTextPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 15f
            isAntiAlias = true
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
        }
        canvas.drawText("SILA KEMUKAKAN KAD INI KEPADA PEGAWAI PENDISPENSIAN SETIAP KALI TERAPI", 185f, 615f, footerTextPaint)

        return bitmap
    }

    /**
     * Saves the generated ID card bitmap to standard Android external files directory.
     */
    fun saveBitmapToStorage(context: android.content.Context, bitmap: Bitmap, fileName: String): String? {
        return try {
            val directory = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            val file = java.io.File(directory, "$fileName.png")
            java.io.FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
