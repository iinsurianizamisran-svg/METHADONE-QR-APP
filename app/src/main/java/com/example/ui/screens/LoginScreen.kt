package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserRoles
import com.example.ui.theme.StatusGreen
import com.example.ui.viewmodel.MethadoneViewModel

enum class AuthMode {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MethadoneViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val clinicSettings by viewModel.clinicSettings.collectAsStateWithLifecycle()
    val clinicNameDisplay = clinicSettings?.clinicName ?: "e-Methadone PKD Kluang"
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    // Form fields
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("admin") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Registration fields
    var regFullName by remember { mutableStateOf("") }
    var regIcStaffId by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regSelectedRole by remember { mutableStateOf(UserRoles.PHARMACY) } // Default Farmasi

    // Forgot Password fields
    var forgotIcStaffId by remember { mutableStateOf("") }
    var forgotUsernameQuery by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }

    // Feedback states
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Header Gradient Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                // KKM Header Branding Logo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "KKM Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "e-Methadone QR",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "$clinicNameDisplay - Program MMT KKM",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Main Auth Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Title Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = when (authMode) {
                                        AuthMode.LOGIN -> "Log Masuk Staf"
                                        AuthMode.REGISTER -> "Pendaftaran Staf Baharu"
                                        AuthMode.FORGOT_PASSWORD -> "Semak & Reset Akses"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (authMode) {
                                        AuthMode.LOGIN -> "Sila masukkan akaun bertugas anda"
                                        AuthMode.REGISTER -> "Pilih peranan (Farmasi, Doktor atau AMO)"
                                        AuthMode.FORGOT_PASSWORD -> "Pemulihan nama pengguna atau kata laluan"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            if (authMode != AuthMode.LOGIN) {
                                IconButton(
                                    onClick = {
                                        authMode = AuthMode.LOGIN
                                        errorMessage = null
                                        successMessage = null
                                    },
                                    modifier = Modifier.testTag("back_to_login_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Kembali ke Log Masuk"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Feedback Messages
                        errorMessage?.let { msg ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        successMessage?.let { msg ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StatusGreen.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StatusGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        when (authMode) {
                            AuthMode.LOGIN -> {
                                // LOGIN FORM
                                Text(
                                    text = "Nama Pengguna (Username)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = username,
                                    onValueChange = {
                                        username = it
                                        errorMessage = null
                                    },
                                    placeholder = { Text("Contoh: admin") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null)
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_username_input")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Kata Laluan (Password)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        errorMessage = null
                                    },
                                    placeholder = { Text("Contoh: admin") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Tukar Kelihatan Kata Laluan"
                                            )
                                        }
                                    },
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_password_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            authMode = AuthMode.FORGOT_PASSWORD
                                            errorMessage = null
                                            successMessage = null
                                        },
                                        modifier = Modifier.testTag("forgot_password_button")
                                    ) {
                                        Text(
                                            text = "Lupa Nama Pengguna / Kata Laluan?",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        isLoading = true
                                        viewModel.login(username, password) { success, msg ->
                                            isLoading = false
                                            if (success) {
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                onLoginSuccess()
                                            } else {
                                                errorMessage = msg
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("login_submit_button")
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "LOG MASUK",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                Spacer(modifier = Modifier.height(16.dp))

                                // Interactive Quick Demo Login Accounts
                                Text(
                                    text = "⚡ Akses Pantas Demo Staf Klinik (Satu Sentuhan):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DemoUserChip(
                                        label = "🔑 Admin (admin / admin)",
                                        roleColor = Color(0xFF6B21A8),
                                        onClick = {
                                            username = "admin"
                                            password = "admin"
                                            viewModel.login("admin", "admin") { success, msg ->
                                                if (success) onLoginSuccess()
                                            }
                                        },
                                        testTag = "demo_admin_chip"
                                    )

                                    DemoUserChip(
                                        label = "💊 Farmasi (farmasi)",
                                        roleColor = Color(0xFF0D9488),
                                        onClick = {
                                            username = "farmasi"
                                            password = "farmasi123"
                                            viewModel.login("farmasi", "farmasi123") { success, msg ->
                                                if (success) onLoginSuccess()
                                            }
                                        },
                                        testTag = "demo_farmasi_chip"
                                    )

                                    DemoUserChip(
                                        label = "👨‍⚕️ Doktor (doktor)",
                                        roleColor = Color(0xFF1E40AF),
                                        onClick = {
                                            username = "doktor"
                                            password = "doktor123"
                                            viewModel.login("doktor", "doktor123") { success, msg ->
                                                if (success) onLoginSuccess()
                                            }
                                        },
                                        testTag = "demo_doktor_chip"
                                    )

                                    DemoUserChip(
                                        label = "🚑 AMO (amo)",
                                        roleColor = Color(0xFFB45309),
                                        onClick = {
                                            username = "amo"
                                            password = "amo123"
                                            viewModel.login("amo", "amo123") { success, msg ->
                                                if (success) onLoginSuccess()
                                            }
                                        },
                                        testTag = "demo_amo_chip"
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                OutlinedButton(
                                    onClick = {
                                        authMode = AuthMode.REGISTER
                                        errorMessage = null
                                        successMessage = null
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("goto_register_button")
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Daftar Akaun Staf Baharu", fontWeight = FontWeight.Bold)
                                }
                            }

                            AuthMode.REGISTER -> {
                                // REGISTER FORM WITH ROLE SELECTION (Farmasi, Doctor, AMO)
                                Text(
                                    text = "1. Pilih Peranan Tugas (Role):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    RoleSelectionChip(
                                        roleName = UserRoles.PHARMACY,
                                        label = "Farmasi",
                                        icon = Icons.Default.LocalHospital,
                                        isSelected = regSelectedRole == UserRoles.PHARMACY,
                                        onClick = { regSelectedRole = UserRoles.PHARMACY },
                                        modifier = Modifier.weight(1f).testTag("role_pharmacy_chip")
                                    )

                                    RoleSelectionChip(
                                        roleName = UserRoles.DOCTOR,
                                        label = "Doktor",
                                        icon = Icons.Default.MedicalServices,
                                        isSelected = regSelectedRole == UserRoles.DOCTOR,
                                        onClick = { regSelectedRole = UserRoles.DOCTOR },
                                        modifier = Modifier.weight(1f).testTag("role_doctor_chip")
                                    )

                                    RoleSelectionChip(
                                        roleName = UserRoles.AMO,
                                        label = "AMO",
                                        icon = Icons.Default.Badge,
                                        isSelected = regSelectedRole == UserRoles.AMO,
                                        onClick = { regSelectedRole = UserRoles.AMO },
                                        modifier = Modifier.weight(1f).testTag("role_amo_chip")
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (regSelectedRole) {
                                        UserRoles.PHARMACY -> "ℹ️ Pegawai Farmasi: Kebenaran penuh dispensasi ubat metadon & inventori stok."
                                        UserRoles.DOCTOR -> "ℹ️ Pegawai Perubatan (Doktor): Kebenaran penilaian klinikal & kelulusan cicir dos."
                                        UserRoles.AMO -> "ℹ️ AMO (Penolong Pegawai Perubatan): Pendaftaran pesakit & imbasan saringan harian."
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text("2. Maklumat Diri & Log Masuk:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = regFullName,
                                    onValueChange = { regFullName = it; errorMessage = null },
                                    label = { Text("Nama Penuh Staf") },
                                    placeholder = { Text("Contoh: Farah Binti Ismail") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("reg_fullname_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = regIcStaffId,
                                    onValueChange = { regIcStaffId = it; errorMessage = null },
                                    label = { Text("No. K/P atau ID Staf KKM") },
                                    placeholder = { Text("Contoh: 920415-10-5124 / FAR-102") },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("reg_staff_id_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = regUsername,
                                    onValueChange = { regUsername = it; errorMessage = null },
                                    label = { Text("Nama Pengguna (Username)") },
                                    placeholder = { Text("Contoh: farah_farmasi") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("reg_username_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = regPassword,
                                    onValueChange = { regPassword = it; errorMessage = null },
                                    label = { Text("Kata Laluan") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = regConfirmPassword,
                                    onValueChange = { regConfirmPassword = it; errorMessage = null },
                                    label = { Text("Sahkan Kata Laluan") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("reg_confirm_password_input")
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        if (regPassword != regConfirmPassword) {
                                            errorMessage = "Kata laluan dan pengesahan kata laluan tidak sepadan."
                                            return@Button
                                        }

                                        isLoading = true
                                        viewModel.registerUser(
                                            fullName = regFullName,
                                            icOrStaffId = regIcStaffId,
                                            username = regUsername,
                                            password = regPassword,
                                            role = regSelectedRole
                                        ) { success, msg ->
                                            isLoading = false
                                            if (success) {
                                                successMessage = msg
                                                username = regUsername
                                                password = regPassword
                                                authMode = AuthMode.LOGIN
                                            } else {
                                                errorMessage = msg
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("submit_register_button")
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("DAFTAR AKAUN STAF ($regSelectedRole)", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                TextButton(
                                    onClick = { authMode = AuthMode.LOGIN },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Sudah ada akaun? Log Masuk Di Sini")
                                }
                            }

                            AuthMode.FORGOT_PASSWORD -> {
                                // FORGOT USERNAME / PASSWORD RESET FORM
                                Text(
                                    text = "Sila masukkan No. K/P / ID Staf atau Nama Pengguna berdaftar untuk pemulihan akses:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = forgotIcStaffId,
                                    onValueChange = { forgotIcStaffId = it; errorMessage = null },
                                    label = { Text("No. K/P atau ID Staf KKM") },
                                    placeholder = { Text("Contoh: 840512-10-5543 / FAR-88102") },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("forgot_ic_staff_id_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = forgotUsernameQuery,
                                    onValueChange = { forgotUsernameQuery = it; errorMessage = null },
                                    label = { Text("Atau Nama Pengguna (Username)") },
                                    placeholder = { Text("Contoh: farmasi / admin") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("forgot_username_query_input")
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Tetapkan Kata Laluan Baharu (Opsional):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = newPasswordInput,
                                    onValueChange = { newPasswordInput = it; errorMessage = null },
                                    label = { Text("Kata Laluan Baharu (Tinggalkan kosong untuk semak nama pengguna sahaja)") },
                                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("forgot_new_password_input")
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        isLoading = true
                                        viewModel.recoverOrResetPassword(
                                            icOrStaffId = forgotIcStaffId,
                                            usernameQuery = forgotUsernameQuery,
                                            newPasswordInput = newPasswordInput
                                        ) { success, msg ->
                                            isLoading = false
                                            if (success) {
                                                successMessage = msg
                                                errorMessage = null
                                            } else {
                                                errorMessage = msg
                                                successMessage = null
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("submit_recovery_button")
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("SEMAK & SET SEMULA AKSES", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                TextButton(
                                    onClick = { authMode = AuthMode.LOGIN },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Kembali ke Log Masuk")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Sistem Pengurusan e-Methadone QR v2.4 • Hak Cipta KKM Malaysia",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RoleSelectionChip(
    roleName: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DemoUserChip(
    label: String,
    roleColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = roleColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, roleColor.copy(alpha = 0.4f)),
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(roleColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = roleColor
            )
        }
    }
}
