package com.example.util

import android.content.Context
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import java.util.concurrent.Executors

@Composable
fun CameraQrScanner(
    onQrCodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isScanningActive: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var lastScannedId by remember { mutableStateOf<String?>(null) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Automatically clear lastScannedId when isScanningActive goes from false to true (meaning user is ready to scan again)
    LaunchedEffect(isScanningActive) {
        if (isScanningActive) {
            lastScannedId = null
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                if (isScanningActive) {
                                    val detected = decodeQrFromImageProxy(imageProxy)
                                    if (detected != null) {
                                        ContextCompat.getMainExecutor(ctx).execute {
                                            val extractedId = QrCodeUtil.extractPatientIdentifier(detected)
                                            lastScannedId = extractedId
                                            onQrCodeDetected(detected)
                                        }
                                    }
                                }
                                imageProxy.close()
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.Builder().requireLensFacing(lensFacing).build(),
                            preview,
                            imageAnalyzer
                        )
                        cameraControl = camera.cameraControl
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            update = {
                // When lensFacing or torch changes, could be handled here if needed
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay with scanning cutout & laser line
        ScannerOverlay()

        // Controls (Torch & Camera Switch)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        ) {
            FilledTonalIconButton(
                onClick = {
                    val nextTorch = !isTorchOn
                    isTorchOn = nextTorch
                    cameraControl?.enableTorch(nextTorch)
                }
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flashlight"
                )
            }
        }

        // Elegant Card Overlay displaying the Scanned Patient ID
        lastScannedId?.let { patientId ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .testTag("scanned_patient_id_card")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Diimbas Berjaya",
                                tint = Color(0xFF10B981), // Emerald/Green color
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "ID PESAKIT DIIMBAS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = patientId,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Clear scan button
                        IconButton(
                            onClick = { lastScannedId = null },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("clear_scanned_id_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Semi-transparent darkened background with clear cutout box
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val boxSize = 260.dp.toPx()
            val left = (canvasWidth - boxSize) / 2
            val top = (canvasHeight - boxSize) / 2

            // Dim everything
            drawRect(
                color = Color.Black.copy(alpha = 0.55f),
                size = size
            )

            // Clear center frame
            val path = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(left, top, left + boxSize, top + boxSize),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )
                )
            }
            drawPath(
                path = path,
                color = Color.Transparent,
                blendMode = BlendMode.Clear
            )

            // High-contrast corners
            val cornerLength = 32.dp.toPx()
            val strokeWidth = 5.dp.toPx()
            val cornerColor = Color(0xFF53DBC9)

            // Top-Left
            drawLine(cornerColor, Offset(left, top + 16.dp.toPx()), Offset(left, top + cornerLength), strokeWidth)
            drawLine(cornerColor, Offset(left + 16.dp.toPx(), top), Offset(left + cornerLength, top), strokeWidth)

            // Top-Right
            drawLine(cornerColor, Offset(left + boxSize, top + 16.dp.toPx()), Offset(left + boxSize, top + cornerLength), strokeWidth)
            drawLine(cornerColor, Offset(left + boxSize - 16.dp.toPx(), top), Offset(left + boxSize - cornerLength, top), strokeWidth)

            // Bottom-Left
            drawLine(cornerColor, Offset(left, top + boxSize - 16.dp.toPx()), Offset(left, top + boxSize - cornerLength), strokeWidth)
            drawLine(cornerColor, Offset(left + 16.dp.toPx(), top + boxSize), Offset(left + cornerLength, top + boxSize), strokeWidth)

            // Bottom-Right
            drawLine(cornerColor, Offset(left + boxSize, top + boxSize - 16.dp.toPx()), Offset(left + boxSize, top + boxSize - cornerLength), strokeWidth)
            drawLine(cornerColor, Offset(left + boxSize - 16.dp.toPx(), top + boxSize), Offset(left + boxSize - cornerLength, top + boxSize), strokeWidth)
        }

        // Scanning animated beam
        Box(
            modifier = Modifier
                .size(width = 240.dp, height = 240.dp)
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, laserY.dp.roundToPx()) }
                    .size(width = 240.dp, height = 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF53DBC9))
            )
        }
    }
}

private fun decodeQrFromImageProxy(image: ImageProxy): String? {
    val plane = image.planes.firstOrNull() ?: return null
    val buffer: ByteBuffer = plane.buffer
    val data = ByteArray(buffer.remaining())
    buffer.get(data)

    val width = image.width
    val height = image.height

    return try {
        val source = PlanarYUVLuminanceSource(
            data,
            width,
            height,
            0,
            0,
            width,
            height,
            false
        )
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader()
        val result = reader.decodeWithState(bitmap)
        result.text
    } catch (e: Exception) {
        null
    }
}
