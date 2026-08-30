package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.InventoryItem
import com.example.data.model.Patient
import com.example.util.QrCodeUtil
import com.example.ui.screens.MonthlyTrendData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("e-Methadone QR", appName)
  }

  @Test
  fun `inventory low stock calculation`() {
    val normalItem = InventoryItem(currentStockMl = 2500.0, reorderThresholdMl = 1000.0)
    assertFalse(normalItem.isLowStock)

    val lowStockItem = InventoryItem(currentStockMl = 850.0, reorderThresholdMl = 1000.0)
    assertTrue(lowStockItem.isLowStock)
  }

  @Test
  fun `inventory volume conversion to liters`() {
    val item = InventoryItem(currentStockMl = 4500.0, reorderThresholdMl = 1200.0)
    assertEquals(4.5, item.currentStockLiters, 0.001)
    assertEquals(1.2, item.reorderThresholdLiters, 0.001)
  }

  @Test
  fun `patient QR payload format verification`() {
    val patient = Patient(
      patientId = "METH-2026-0999",
      name = "Zulkifli Ibrahim",
      icNumber = "900101-14-5566",
      currentDoseMg = 75.0,
      doseVolumeMl = 15.0,
      dispenseType = "TAKE_HOME",
      registrationDate = "2026-08-30"
    )

    val expectedPayload = "METH_QR|METH-2026-0999|900101-14-5566|Zulkifli Ibrahim|75.0|TAKE_HOME"
    assertEquals(expectedPayload, patient.toQrPayload())
  }

  @Test
  fun `qr bitmap generation succeeds for patient payload`() {
    val payload = "METH_QR|METH-2026-0123|880512-10-5543|Ahmad Razali|60.0|DOT"
    val bitmap = QrCodeUtil.generateQrBitmap(payload, 300)
    assertNotNull(bitmap)
    assertEquals(300, bitmap?.width)
    assertEquals(300, bitmap?.height)
  }

  @Test
  fun `monthly trend data metrics calculation`() {
    val trend = MonthlyTrendData(
      month = "Ogos",
      year = 2026,
      dotAttendance = 440,
      takeHomeAttendance = 200,
      totalVolumeMl = 12800.0,
      totalDoseMg = 64000.0,
      complianceRate = 95.2
    )

    assertEquals(640, trend.totalAttendance)
    assertEquals(12.8, trend.totalVolumeLiters, 0.001)
  }

  @Test
  fun `missed dose alert threshold filtering`() {
    val patients = listOf(
      Patient(patientId = "1", name = "Pesakit Normal", missedDaysStreak = 0, icNumber = "111111-11-1111"),
      Patient(patientId = "2", name = "Cicir 2 Hari", missedDaysStreak = 2, icNumber = "222222-22-2222"),
      Patient(patientId = "3", name = "Cicir 4 Hari Alert", missedDaysStreak = 4, icNumber = "333333-33-3333"),
      Patient(patientId = "4", name = "Cicir 5 Hari Alert", missedDaysStreak = 5, icNumber = "444444-44-4444")
    )

    val flaggedAlerts = patients.filter { it.status == "AKTIF" && it.missedDaysStreak > 3 }
    assertEquals(2, flaggedAlerts.size)
    assertTrue(flaggedAlerts.any { it.patientId == "3" })
    assertTrue(flaggedAlerts.any { it.patientId == "4" })
  }
}

