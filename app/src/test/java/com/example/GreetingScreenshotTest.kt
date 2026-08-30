package com.example

import com.example.data.model.Patient
import com.example.util.QrCodeUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MethadoneAppUnitTest {

  @Test
  fun testPatientQrPayloadExtraction() {
    val patient = Patient(
      patientId = "METH-2026-0012",
      name = "Ahmad Razali",
      icNumber = "840512-10-5543",
      currentDoseMg = 75.0,
      doseVolumeMl = 15.0,
      registrationDate = "2024-03-10"
    )

    val payload = patient.toQrPayload()
    val extractedId = QrCodeUtil.extractPatientIdentifier(payload)
    assertEquals("METH-2026-0012", extractedId)
  }

  @Test
  fun testDirectIdOrIcExtraction() {
    val extractedDirect = QrCodeUtil.extractPatientIdentifier("METH-2026-0034")
    assertEquals("METH-2026-0034", extractedDirect)

    val extractedIc = QrCodeUtil.extractPatientIdentifier("840512-10-5543")
    assertEquals("840512-10-5543", extractedIc)
  }

  @Test
  fun testQrCodeBitmapGeneration() {
    val bitmap = QrCodeUtil.generateQrBitmap("METH-2026-0012", 200)
    assertNotNull(bitmap)
    assertEquals(200, bitmap?.width)
    assertEquals(200, bitmap?.height)
  }
}

