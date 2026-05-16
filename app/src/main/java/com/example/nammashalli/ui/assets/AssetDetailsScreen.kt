package com.nammashalli.inventory.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nammashalli.inventory.ui.common.*
import com.nammashalli.inventory.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AssetDetailsScreen(
    assetId: Long,
    onBack: () -> Unit,
    onNavigateToHealthCheck: () -> Unit,
    onNavigateToIssueLog: () -> Unit,
    viewModel: AssetViewModel = hiltViewModel()
) {
    val asset by viewModel.detailAsset.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(assetId) { viewModel.loadAssetDetail(assetId) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Asset") },
            text = { Text("Are you sure you want to delete this asset? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    asset?.let { viewModel.deleteAsset(it) }
                    showDeleteDialog = false
                    onBack()
                }) { Text("Delete", color = StatusRed) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = asset?.assetName ?: "Asset Details",
                onBack = onBack
            ) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                }
            }
        }
    ) { padding ->
        val a = asset
        if (a == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Photo
                if (a.photoPath != null) {
                    AsyncImage(
                        model = a.photoPath,
                        contentDescription = "Asset photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(220.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp).background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory, null, tint = GreenPrimary, modifier = Modifier.size(48.dp))
                            Text("No photo available", color = TextSecondary)
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // Status header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(a.assetName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(a.assetId, style = MaterialTheme.typography.bodyMedium, color = GreenPrimary, fontWeight = FontWeight.Medium)
                        }
                        StatusBadge(a.currentStatus)
                    }
                    Spacer(Modifier.height(16.dp))

                    // Details card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DetailRow(Icons.Default.Category, "Category", a.category)
                            DetailRow(Icons.Default.LocationOn, "Location", a.location)
                            a.serialNumber?.let { DetailRow(Icons.Default.Numbers, "Serial Number", it) }
                            a.estimatedCost?.let { DetailRow(Icons.Default.CurrencyRupee, "Estimated Cost", "₹${String.format("%.2f", it)}") }
                            a.purchaseDate?.let {
                                DetailRow(Icons.Default.CalendarToday, "Purchase Date",
                                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it)))
                            }
                            a.assignedTo?.let { DetailRow(Icons.Default.Person, "Assigned To", it) }
                            a.description?.let { DetailRow(Icons.Default.Notes, "Description", it) }
                            DetailRow(Icons.Default.AccessTime, "Registered",
                                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(a.createdAt)))
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Action buttons
                    SectionHeader("Actions")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onNavigateToHealthCheck,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Icon(Icons.Default.HealthAndSafety, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Health Check")
                        }
                        Button(
                            onClick = onNavigateToIssueLog,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusYellow)
                        ) {
                            Icon(Icons.Default.ReportProblem, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Log Issue")
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(20.dp).padding(top = 1.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
