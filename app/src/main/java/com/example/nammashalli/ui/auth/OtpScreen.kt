package com.nammashalli.inventory.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nammashalli.inventory.ui.common.AppTopBar
import com.nammashalli.inventory.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun OtpScreen(
    phone: String,
    generatedOtp: String,
    onNavigateToDashboard: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.otpState.collectAsStateWithLifecycle()

    LaunchedEffect(phone) {
        viewModel.initOtpState(phone, generatedOtp)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            viewModel.tickOtp()
        }
    }

    LaunchedEffect(state.success) {
        if (state.success) onNavigateToDashboard()
    }

    Scaffold(
        topBar = { AppTopBar("OTP Verification", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(GreenLight, RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Sms, null, tint = GreenPrimary, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("Verification Code", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Enter the 6-digit code sent to\n+91 ${state.phone}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            // Demo hint
            if (generatedOtp.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusYellowBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = StatusYellow, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Demo OTP: $generatedOtp", color = StatusYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))

            // OTP input
            OtpInputField(
                otp = state.otp,
                onOtpChange = { viewModel.updateOtp(it) }
            )

            Spacer(Modifier.height(16.dp))

            // Timer
            val minutes = state.remainingSeconds / 60
            val seconds = state.remainingSeconds % 60
            val timerColor = if (state.remainingSeconds < 60) StatusRed else GreenPrimary
            Text(
                text = "Expires in: ${String.format("%02d:%02d", minutes, seconds)}",
                color = timerColor,
                fontWeight = FontWeight.Medium
            )

            state.error?.let {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, null, tint = StatusRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(it, color = StatusRed, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { viewModel.verifyOtp() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !state.isLoading && state.otp.length == 6,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Verify OTP", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { viewModel.resendOtp() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !state.isLoading && state.remainingSeconds == 0L,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Resend OTP")
            }
        }
    }
}

@Composable
private fun OtpInputField(otp: String, onOtpChange: (String) -> Unit) {
    BasicTextField(
        value = otp,
        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onOtpChange(it) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(6) { index ->
                    val char = otp.getOrNull(index)
                    val isFocused = index == otp.length
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (char != null) GreenLight else Color.White,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) GreenPrimary else DividerColor,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char?.toString() ?: "",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}
