package com.nammashalli.inventory.ui.healthcheck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nammashalli.inventory.ui.common.AppTopBar
import com.nammashalli.inventory.ui.common.StatusBadge
import com.nammashalli.inventory.ui.navigation.Screen
import com.nammashalli.inventory.ui.theme.*

@Composable
fun HealthCheckSummaryScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    navController: NavController
) {
    val parentEntry = remember(navController) {
        navController.getBackStackEntry(Screen.HealthCheckSelect.route)
    }
    val viewModel: HealthCheckViewModel = hiltViewModel(parentEntry)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val elapsedSec = state.elapsedMs / 1000
    val isUnder2Min = elapsedSec < 120L

    LaunchedEffect(state.submitted) {
        if (state.submitted) onDone()
    }

    Scaffold(
        topBar = { AppTopBar("Health Check Summary", onBack = onBack) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Time card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isUnder2Min) GreenLight else StatusYellowBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isUnder2Min) Icons.Default.FlashOn else Icons.Default.Timer,
                        null,
                        tint = if (isUnder2Min) GreenPrimary else StatusYellow,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        val mins = elapsedSec / 60
                        val secs = elapsedSec % 60
                        Text(
                            "Time: ${if (mins > 0) "${mins}m " else ""}${secs}s",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isUnder2Min) GreenPrimary else StatusYellow
                        )
                        Text(
                            if (isUnder2Min) "Under 2 minutes! Great work!" else "Keep practicing for faster checks!",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isUnder2Min) GreenPrimary else StatusYellow
                        )
                    }
                }
            }

            // Status counts
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val counts = mapOf(
                    "Good" to state.checkItems.count { it.status == "Good" },
                    "Fair" to state.checkItems.count { it.status == "Fair" },
                    "NeedsRepair" to state.checkItems.count { it.status == "NeedsRepair" },
                    "Lost" to state.checkItems.count { it.status == "Lost" }
                )
                counts.forEach { (status, count) ->
                    if (count > 0) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = com.nammashalli.inventory.ui.common.AssetStatus.fromString(status).bgColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp,
                                    color = com.nammashalli.inventory.ui.common.AssetStatus.fromString(status).color)
                                Text(status.replace("NeedsRepair", "Repair"), fontSize = 10.sp,
                                    color = com.nammashalli.inventory.ui.common.AssetStatus.fromString(status).color)
                            }
                        }
                    }
                }
            }

            val redCount = state.checkItems.count { it.status == "NeedsRepair" }
            if (redCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusRedBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = StatusRed, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("$redCount repair request(s) will be auto-created", color = StatusRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.checkItems) { item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.asset.assetName, fontWeight = FontWeight.Medium)
                                Text(item.asset.assetId, style = MaterialTheme.typography.labelSmall, color = GreenPrimary)
                                if (item.notes.isNotBlank()) {
                                    Text(item.notes, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            StatusBadge(item.status)
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }

            Surface(shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { viewModel.submitHealthCheck() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Submit Health Check", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
