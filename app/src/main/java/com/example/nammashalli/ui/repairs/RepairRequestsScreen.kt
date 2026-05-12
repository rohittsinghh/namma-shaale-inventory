package com.example.nammashalli.ui.repairs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.nammashalli.ui.common.AppTopBar
import com.example.nammashalli.ui.common.EmptyState
import com.example.nammashalli.ui.common.RepairPriority
import com.example.nammashalli.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RepairRequestsScreen(
    onBack: (() -> Unit)? = null,
    viewModel: RepairViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filters = listOf("Pending", "Completed", "All")

    Scaffold(
        topBar = { AppTopBar("Repair Requests", onBack = onBack) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Stats strip
            if (!state.isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (state.repairs.any { it.status == "Pending" }) StatusRedBg else GreenLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val pending = state.repairs.count { it.status == "Pending" }
                        val completed = state.repairs.count { it.status == "Completed" }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(pending.toString(), fontWeight = FontWeight.Bold, color = StatusRed, style = MaterialTheme.typography.titleLarge)
                            Text("Pending", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        VerticalDivider(modifier = Modifier.height(36.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(completed.toString(), fontWeight = FontWeight.Bold, color = StatusGreen, style = MaterialTheme.typography.titleLarge)
                            Text("Completed", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = state.filterStatus == filter,
                        onClick = { viewModel.filterByStatus(filter) },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else if (state.repairs.isEmpty()) {
                EmptyState("No repair requests", Icons.Default.Build)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.repairs, key = { it.id }) { repair ->
                        val asset = state.assets[repair.assetId]
                        val priorityColor = RepairPriority.colorFor(repair.priority)
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(asset?.assetName ?: "Unknown Asset", fontWeight = FontWeight.SemiBold)
                                        Text(asset?.assetId ?: "", style = MaterialTheme.typography.labelSmall, color = GreenPrimary)
                                    }
                                    // Priority badge
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = when (repair.priority) {
                                                "High" -> StatusRedBg
                                                "Medium" -> StatusYellowBg
                                                else -> StatusGreenBg
                                            }
                                        ),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            repair.priority,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = priorityColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(repair.reason, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Reported: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(repair.requestedAt))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                        if (repair.status == "Completed" && repair.completedAt != null) {
                                            Text(
                                                "Completed: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(repair.completedAt))}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = StatusGreen
                                            )
                                        }
                                    }
                                    if (repair.status == "Pending") {
                                        Button(
                                            onClick = { viewModel.markCompleted(repair.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                            modifier = Modifier.height(36.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Done", style = MaterialTheme.typography.labelMedium)
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Completed", style = MaterialTheme.typography.labelSmall, color = StatusGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}
