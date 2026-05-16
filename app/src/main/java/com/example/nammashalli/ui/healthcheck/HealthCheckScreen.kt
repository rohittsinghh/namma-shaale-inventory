package com.nammashalli.inventory.ui.healthcheck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nammashalli.inventory.ui.navigation.Screen
import com.nammashalli.inventory.ui.theme.*

@Composable
fun HealthCheckScreen(
    onNavigateToSummary: () -> Unit,
    onBack: () -> Unit,
    navController: NavController
) {
    val parentEntry = remember(navController) {
        navController.getBackStackEntry(Screen.HealthCheckSelect.route)
    }
    val viewModel: HealthCheckViewModel = hiltViewModel(parentEntry)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items = state.checkItems
    val index = state.currentIndex
    val isLast = index >= items.size - 1

    if (items.isEmpty()) {
        onBack()
        return
    }

    val current = items.getOrNull(index) ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
                Text(
                    "${index + 1} / ${items.size}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(48.dp))
            }

            LinearProgressIndicator(
                progress = { (index + 1).toFloat() / items.size },
                modifier = Modifier.fillMaxWidth().height(6.dp).padding(horizontal = 4.dp),
                color = GreenPrimary,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(24.dp))

            // Asset info
            Text(
                current.asset.assetName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "${current.asset.assetId} • ${current.asset.category}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Text(
                current.asset.location,
                color = GreenSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(32.dp))

            // Current status display
            Text("Current: ${current.asset.currentStatus}", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Text("Set new status:", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(16.dp))

            // Big status buttons - optimized for speed
            StatusButton(
                label = "GOOD",
                emoji = "✓",
                color = StatusGreen,
                bgColor = StatusGreenBg.copy(alpha = 0.15f),
                isSelected = current.status == "Good",
                onClick = { viewModel.setStatus("Good") }
            )
            Spacer(Modifier.height(12.dp))
            StatusButton(
                label = "FAIR",
                emoji = "~",
                color = StatusYellow,
                bgColor = StatusYellowBg.copy(alpha = 0.15f),
                isSelected = current.status == "Fair",
                onClick = { viewModel.setStatus("Fair") }
            )
            Spacer(Modifier.height(12.dp))
            StatusButton(
                label = "NEEDS REPAIR",
                emoji = "!",
                color = StatusRed,
                bgColor = StatusRedBg.copy(alpha = 0.15f),
                isSelected = current.status == "NeedsRepair",
                onClick = { viewModel.setStatus("NeedsRepair") }
            )
            Spacer(Modifier.height(12.dp))
            StatusButton(
                label = "LOST",
                emoji = "✗",
                color = Color.White.copy(alpha = 0.7f),
                bgColor = Color.White.copy(alpha = 0.08f),
                isSelected = current.status == "Lost",
                onClick = { viewModel.setStatus("Lost") }
            )

            Spacer(Modifier.height(20.dp))

            // Notes (minimal, non-blocking)
            OutlinedTextField(
                value = current.notes,
                onValueChange = { viewModel.setNotes(it) },
                placeholder = { Text("Optional notes...", color = Color.White.copy(alpha = 0.4f)) },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                ),
                singleLine = true
            )

            Spacer(Modifier.weight(1f))

            // Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (index > 0) {
                    OutlinedButton(
                        onClick = { viewModel.previous() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder().copy(width = 1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Back")
                    }
                }
                Button(
                    onClick = {
                        if (isLast) onNavigateToSummary() else viewModel.next()
                    },
                    modifier = Modifier.weight(if (index > 0) 2f else 1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isLast) "Review Summary" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isLast) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, null)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusButton(
    label: String,
    emoji: String,
    color: Color,
    bgColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color.copy(alpha = 0.3f) else bgColor,
            contentColor = color
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder().copy(width = 3.dp) else null
    ) {
        Text(emoji, fontSize = 22.sp, modifier = Modifier.padding(end = 12.dp))
        Text(
            label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        if (isSelected) {
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.Check, null, tint = color, modifier = Modifier.size(22.dp))
        }
    }
}
