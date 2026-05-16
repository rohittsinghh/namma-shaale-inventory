package com.nammashalli.inventory.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nammashalli.inventory.ui.theme.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.updateProfilePhoto(context, it) }
    }

    var showEditName by remember { mutableStateOf(false) }
    var showEditEmail by remember { mutableStateOf(false) }
    var showEditPhone by remember { mutableStateOf(false) }
    var showEditSchool by remember { mutableStateOf(false) }
    var showRolePicker by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showRevealConfirm by remember { mutableStateOf(false) }

    // Single stable collector — never cancelled by recomposition
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.messages.collect { msg ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg.text)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ─── Section 1: Profile Header ──────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(GreenPrimary, GreenDark)))
                        .padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            val photoMod = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                            if (state.profilePhotoPath != null) {
                                AsyncImage(
                                    model = state.profilePhotoPath,
                                    contentDescription = "Profile photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = photoMod
                                )
                            } else {
                                Box(
                                    modifier = photoMod.background(GreenSecondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(60.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White, CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Edit, null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            state.fullName.ifBlank { "—" },
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            state.role.ifBlank { "—" },
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Text(
                            state.schoolName.ifBlank { "—" },
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                        if (state.isSaving) {
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.width(120.dp),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            // ─── Section 2: Personal Information ───────────────────────────
            item {
                ProfileSectionHeader("Personal Information")
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        InfoRow(Icons.Default.Person, "Full Name", state.fullName) { showEditName = true }
                        HorizontalDivider(color = DividerColor)
                        InfoRow(Icons.Default.Email, "Email", state.email) { showEditEmail = true }
                        HorizontalDivider(color = DividerColor)
                        InfoRow(Icons.Default.Phone, "Phone", state.phone) { showEditPhone = true }
                        HorizontalDivider(color = DividerColor)
                        InfoRow(Icons.Default.Badge, "Role", state.role) { showRolePicker = true }
                        HorizontalDivider(color = DividerColor)
                        InfoRow(Icons.Default.School, "School", state.schoolName) { showEditSchool = true }
                    }
                }
            }

            // ─── Section 3: Groq API Key ────────────────────────────────────
            item {
                ProfileSectionHeader("Groq AI Integration")
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Groq API Key", fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Spacer(Modifier.weight(1f))
                            ApiKeyStatusChip(state.apiKeyValid)
                        }
                        Spacer(Modifier.height(10.dp))

                        val rawApiKey = state.rawApiKey
                        val maskedKey = when {
                            rawApiKey.isNullOrBlank() -> "Not configured"
                            state.apiKeyVisible -> rawApiKey
                            rawApiKey.length > 4 ->
                                "••••••••••••••••••••" + rawApiKey.takeLast(4)
                            else -> "••••"
                        }
                        Text(
                            maskedKey,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = if (rawApiKey.isNullOrBlank()) TextSecondary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!state.rawApiKey.isNullOrBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        if (state.apiKeyVisible) viewModel.setApiKeyVisible(false)
                                        else showRevealConfirm = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary)
                                ) {
                                    Icon(
                                        if (state.apiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        null, modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (state.apiKeyVisible) "Hide" else "Show", fontSize = 12.sp)
                                }
                            }
                            if (!state.rawApiKey.isNullOrBlank()) {
                                Button(
                                    onClick = { viewModel.testApiKey() },
                                    enabled = !state.isTestingApiKey,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenSecondary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    if (state.isTestingApiKey) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.NetworkCheck, null, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    Text("Test", fontSize = 12.sp)
                                }
                            }
                            Button(
                                onClick = { showApiKeyDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.VpnKey, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (state.rawApiKey.isNullOrBlank()) "Set Key" else "Update", fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Get your free API key at console.groq.com",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.groq.com"))
                                    context.startActivity(intent)
                                }
                        )
                    }
                }
            }

            // ─── Section 4: Settings ────────────────────────────────────────
            item {
                ProfileSectionHeader("Settings")
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        // Theme selector
                        SettingsRow(Icons.Default.Palette, "Theme") {
                            val themes = listOf("Light", "Dark", "System")
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { expanded = true }) {
                                    Text(state.theme, color = GreenPrimary, fontSize = 13.sp)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = GreenPrimary)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    themes.forEach { t ->
                                        DropdownMenuItem(
                                            text = { Text(t) },
                                            onClick = { viewModel.setTheme(t); expanded = false }
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = DividerColor)
                        SettingsRow(Icons.Default.Notifications, "Notifications") {
                            Switch(
                                checked = state.notificationsEnabled,
                                onCheckedChange = { viewModel.setNotifications(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = GreenPrimary, checkedTrackColor = GreenLight)
                            )
                        }
                        HorizontalDivider(color = DividerColor)
                        SettingsRow(Icons.Default.DeleteSweep, "Clear Cache") {
                            TextButton(onClick = { viewModel.clearCache(context) }) {
                                Text("Clear", color = StatusRed, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // ─── Section 5: Logout ──────────────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Logout", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────

    if (showEditName) {
        EditFieldDialog(
            title = "Edit Full Name",
            initialValue = state.fullName,
            onDismiss = { showEditName = false },
            onSave = { viewModel.updateFullName(it); showEditName = false }
        )
    }
    if (showEditEmail) {
        EditFieldDialog(
            title = "Edit Email",
            initialValue = state.email,
            keyboardType = KeyboardType.Email,
            onDismiss = { showEditEmail = false },
            onSave = { viewModel.updateEmail(it); showEditEmail = false }
        )
    }
    if (showEditPhone) {
        EditFieldDialog(
            title = "Edit Phone Number",
            initialValue = state.phone.filter { it.isDigit() }.takeLast(10),
            keyboardType = KeyboardType.Phone,
            hint = "10-digit mobile number",
            onDismiss = { showEditPhone = false },
            onSave = { viewModel.updatePhone(it); showEditPhone = false }
        )
    }
    if (showEditSchool) {
        EditFieldDialog(
            title = "Edit School Name",
            initialValue = state.schoolName,
            onDismiss = { showEditSchool = false },
            onSave = { viewModel.updateSchool(it); showEditSchool = false }
        )
    }
    if (showRolePicker) {
        RolePickerDialog(
            currentRole = state.role,
            onDismiss = { showRolePicker = false },
            onSelect = { viewModel.updateRole(it); showRolePicker = false }
        )
    }
    if (showApiKeyDialog) {
        UpdateApiKeyDialog(
            isLoading = state.isTestingApiKey || state.isSaving,
            onDismiss = { showApiKeyDialog = false },
            onTest = { key -> viewModel.testApiKey(key) },
            onSave = { key -> viewModel.saveApiKey(key); showApiKeyDialog = false }
        )
    }
    if (showRevealConfirm) {
        AlertDialog(
            onDismissRequest = { showRevealConfirm = false },
            icon = { Icon(Icons.Default.Warning, null, tint = StatusYellow) },
            title = { Text("Reveal API Key?") },
            text = { Text("Your API key will be visible for 10 seconds and then hidden again.") },
            confirmButton = {
                Button(onClick = { viewModel.setApiKeyVisible(true); showRevealConfirm = false }) {
                    Text("Show Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevealConfirm = false }) { Text("Cancel") }
            }
        )
    }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null, tint = StatusRed) },
            title = { Text("Logout?") },
            text = { Text("You will be logged out and all session data cleared.") },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; viewModel.logout(onLogout) },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = GreenPrimary,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value.ifBlank { "—" }, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp)
        trailing()
    }
}

@Composable
private fun ApiKeyStatusChip(valid: Boolean?) {
    val (text, color, bg) = when (valid) {
        true -> Triple("Valid", StatusGreen, StatusGreenBg)
        false -> Triple("Invalid", StatusRed, StatusRedBg)
        null -> Triple("Not tested", StatusYellow, StatusYellowBg)
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EditFieldDialog(
    title: String,
    initialValue: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    hint: String = "",
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                placeholder = { if (hint.isNotBlank()) Text(hint, color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    focusedLabelColor = GreenPrimary
                )
            )
        },
        confirmButton = {
            Button(onClick = { onSave(text) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RolePickerDialog(
    currentRole: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val roles = listOf("Teacher", "Principal", "SDMC Member", "Admin")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Role", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                roles.forEach { role ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(role) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = role == currentRole,
                            onClick = { onSelect(role) },
                            colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(role)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun UpdateApiKeyDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onTest: (String) -> Unit,
    onSave: (String) -> Unit
) {
    var apiKey by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Groq API Key", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Paste your API key here", color = TextSecondary) },
                    singleLine = true,
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = TextSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Get your key at console.groq.com",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onTest(apiKey) },
                    enabled = apiKey.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Test")
                    }
                }
                Button(
                    onClick = { onSave(apiKey) },
                    enabled = apiKey.isNotBlank() && !isLoading
                ) {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
