package com.example.nammashalli.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nammashalli.ui.common.UserRole
import com.example.nammashalli.ui.theme.*
import com.example.nammashalli.utils.PasswordStrength

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToSignIn: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.signUpState.collectAsStateWithLifecycle()
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var roleExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.success) {
        if (state.success) onNavigateToSignIn()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(GreenLight, RoundedCornerShape(36.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Namma-Shaale Inventory", style = MaterialTheme.typography.headlineMedium, color = GreenPrimary, fontWeight = FontWeight.Bold)
            Text("Create your account", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(32.dp))

            // Form fields
            OutlinedTextField(
                value = state.fullName,
                onValueChange = { viewModel.updateSignUpField(SignUpField.FULL_NAME, it) },
                label = { Text("Full Name *") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { if (state.fullName.isNotBlank() && state.fullName.length > 50) Text("Max 50 characters", color = StatusRed) }
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.updateSignUpField(SignUpField.EMAIL, it) },
                label = { Text("Email Address *") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.phone,
                onValueChange = { viewModel.updateSignUpField(SignUpField.PHONE, it) },
                label = { Text("Phone Number *") },
                leadingIcon = { Text("+91 ", modifier = Modifier.padding(start = 8.dp), color = GreenPrimary, fontWeight = FontWeight.SemiBold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("10-digit mobile number") }
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.updateSignUpField(SignUpField.PASSWORD, it) },
                label = { Text("Password *") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            AnimatedVisibility(state.password.isNotBlank()) {
                PasswordStrengthIndicator(state.passwordStrength)
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { viewModel.updateSignUpField(SignUpField.CONFIRM_PASSWORD, it) },
                label = { Text("Confirm Password *") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = {
                    if (state.confirmPassword.isNotBlank() && state.password != state.confirmPassword) {
                        Text("Passwords do not match", color = StatusRed)
                    }
                }
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.schoolName,
                onValueChange = { viewModel.updateSignUpField(SignUpField.SCHOOL_NAME, it) },
                label = { Text("School Name *") },
                leadingIcon = { Icon(Icons.Default.School, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            // Role dropdown
            ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = it }) {
                OutlinedTextField(
                    value = state.role,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Role *") },
                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                    UserRole.all().forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role) },
                            onClick = { viewModel.updateRole(role); roleExpanded = false }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.groqApiKey,
                onValueChange = { viewModel.updateSignUpField(SignUpField.GROQ_API_KEY, it) },
                label = { Text("Groq API Key (Optional)") },
                leadingIcon = { Icon(Icons.Default.Key, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("For AI insights - optional") }
            )
            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = state.termsAccepted,
                    onCheckedChange = { viewModel.updateTerms(it) }
                )
                Text(
                    text = "I agree to the Terms & Conditions and Privacy Policy",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.signUp() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !state.isLoading && state.termsAccepted,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onNavigateToSignIn) {
                Text("Already have an account? ", color = TextSecondary)
                Text("Sign In", color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }

        // Error snackbar
        state.error?.let { error ->
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = StatusRedBg)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, null, tint = StatusRed)
                    Spacer(Modifier.width(8.dp))
                    Text(error, color = StatusRed, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.clearSignUpError() }) {
                        Icon(Icons.Default.Close, null, tint = StatusRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val (color, label) = when (strength) {
        PasswordStrength.WEAK -> Pair(StatusRed, "Weak")
        PasswordStrength.MODERATE -> Pair(StatusYellow, "Moderate")
        PasswordStrength.GOOD -> Pair(StatusGreen, "Good")
        PasswordStrength.STRONG -> Pair(GreenPrimary, "Strong")
    }
    val progress = when (strength) {
        PasswordStrength.WEAK -> 0.25f
        PasswordStrength.MODERATE -> 0.5f
        PasswordStrength.GOOD -> 0.75f
        PasswordStrength.STRONG -> 1f
    }
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = color,
            trackColor = DividerColor
        )
        Text("Password strength: $label", fontSize = 11.sp, color = color, modifier = Modifier.padding(top = 2.dp))
    }
}
