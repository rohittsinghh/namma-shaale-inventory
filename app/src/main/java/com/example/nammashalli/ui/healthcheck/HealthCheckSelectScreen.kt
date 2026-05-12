package com.example.nammashalli.ui.healthcheck

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.nammashalli.ui.common.AppTopBar
import com.example.nammashalli.ui.common.EmptyState
import com.example.nammashalli.ui.common.StatusBadge
import com.example.nammashalli.ui.theme.*

@Composable
fun HealthCheckSelectScreen(
    onBack: () -> Unit,
    onStartCheck: () -> Unit,
    navController: NavController,
    viewModel: HealthCheckViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppTopBar("Select Assets", onBack = onBack) },
        bottomBar = {
            if (state.selectedIds.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "${state.selectedIds.size} asset(s) selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = { viewModel.startHealthCheck(); onStartCheck() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Start Health Check", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Select all / none
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = GreenLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Select assets to check", fontWeight = FontWeight.SemiBold, color = GreenPrimary)
                        Text("Target: 12 seconds per asset", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.selectAll() },
                            border = ButtonDefaults.outlinedButtonBorder().copy(width = 1.dp),
                            modifier = Modifier.height(36.dp)
                        ) { Text("All") }
                        OutlinedButton(
                            onClick = { viewModel.selectNone() },
                            border = ButtonDefaults.outlinedButtonBorder().copy(width = 1.dp),
                            modifier = Modifier.height(36.dp)
                        ) { Text("None") }
                    }
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else if (state.assets.isEmpty()) {
                EmptyState("No assets to check", Icons.Default.Inventory)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.assets, key = { it.id }) { asset ->
                        val isSelected = state.selectedIds.contains(asset.id)
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleSelect(asset.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) GreenLight else Color.White
                            ),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(width = 2.dp) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { viewModel.toggleSelect(asset.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = GreenPrimary)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(asset.assetName, fontWeight = FontWeight.SemiBold)
                                    Text("${asset.assetId} • ${asset.location}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                StatusBadge(asset.currentStatus)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}
