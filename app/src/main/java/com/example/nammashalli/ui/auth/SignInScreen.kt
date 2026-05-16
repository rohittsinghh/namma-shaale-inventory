package com.nammashalli.inventory.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nammashalli.inventory.ui.theme.*

@Composable
fun SignInScreen(
    onNavigateToOtp: (String, String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.signInState.collectAsStateWithLifecycle()

    LaunchedEffect(state.otpSent) {
        if (state.otpSent) {
            onNavigateToOtp(state.cleanPhone, state.generatedOtp)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(GreenLight, RoundedCornerShape(44.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(52.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("Welcome Back!", style = MaterialTheme.typography.headlineMedium, color = GreenPrimary, fontWeight = FontWeight.Bold)
            Text("Sign in to Namma-Shaale Inventory", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Enter your phone number", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("We'll send you a verification code", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = state.phone,
                        onValueChange = { viewModel.updatePhone(it) },
                        label = { Text("Phone Number") },
                        leadingIcon = {
                            Text("+91 ", modifier = Modifier.padding(start = 8.dp), color = GreenPrimary, fontWeight = FontWeight.SemiBold)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("10-digit mobile number") },
                        isError = state.error != null
                    )

                    state.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, null, tint = StatusRed, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(it, color = StatusRed, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.sendOtp() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !state.isLoading && state.phone.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            Spacer(Modifier.width(8.dp))
                        }
                        Icon(Icons.Default.Sms, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Send OTP", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            TextButton(onClick = onNavigateToSignUp) {
                Text("Don't have an account? ", color = TextSecondary)
                Text("Register", color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
