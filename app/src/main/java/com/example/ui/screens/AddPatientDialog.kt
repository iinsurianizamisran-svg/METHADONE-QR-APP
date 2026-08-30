package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientDialog(
    onDismiss: () -> Unit,
    onAddPatient: (
        name: String,
        ic: String,
        phone: String,
        dose: Double,
        dispenseType: String,
        takeHomeDays: Int,
        doctor: String,
        clinic: String,
        notes: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var icNumber by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("01") }
    var doseMgText by remember { mutableStateOf("60") }
    var dispenseType by remember { mutableStateOf("DOT") }
    var takeHomeDaysText by remember { mutableStateOf("2") }
    var doctorName by remember { mutableStateOf("Dr. Farah Hanim (FMS)") }
    var clinicLocation by remember { mutableStateOf("Klinik Kesihatan Cheras") }
    var notes by remember { mutableStateOf("Pesakit patuh. Memulakan program Terapi Gentian Metadon.") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pendaftaran Pesakit Baharu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Penuh Pesakit *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_name_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = icNumber,
                onValueChange = { icNumber = it },
                label = { Text("No. Kad Pengenalan (cth: 880512-10-5543) *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_ic_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("No. Telefon / Waris") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = doseMgText,
                onValueChange = { doseMgText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Dos Permulaan (mg) * (Standard: 5mg = 1mL)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_dose_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Kategori Pengambilan Ubat:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { dispenseType = "DOT" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (dispenseType == "DOT") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                ) {
                    Text("DOT (Klinik)")
                }

                OutlinedButton(
                    onClick = { dispenseType = "TAKE_HOME" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (dispenseType == "TAKE_HOME") MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent
                    )
                ) {
                    Text("Bawa Balik")
                }
            }

            if (dispenseType == "TAKE_HOME") {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = takeHomeDaysText,
                    onValueChange = { takeHomeDaysText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Kelayakan Hari Bawa Balik (1 - 7 hari)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = doctorName,
                onValueChange = { doctorName = it },
                label = { Text("Pegawai Perubatan / Pakar Penilai") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = clinicLocation,
                onValueChange = { clinicLocation = it },
                label = { Text("Pusat Rawatan / Klinik Kesihatan") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan Klinikal") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Sila masukkan nama penuh pesakit."
                        return@Button
                    }
                    if (icNumber.isBlank()) {
                        errorMessage = "Sila masukkan No. Kad Pengenalan."
                        return@Button
                    }
                    val dose = doseMgText.toDoubleOrNull() ?: 0.0
                    if (dose <= 0) {
                        errorMessage = "Sila masukkan dos yang sah."
                        return@Button
                    }

                    val days = takeHomeDaysText.toIntOrNull() ?: 0

                    onAddPatient(
                        name,
                        icNumber,
                        phoneNumber,
                        dose,
                        dispenseType,
                        days,
                        doctorName,
                        clinicLocation,
                        notes
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_patient_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Daftar Pesakit & Jana Kod QR", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}
