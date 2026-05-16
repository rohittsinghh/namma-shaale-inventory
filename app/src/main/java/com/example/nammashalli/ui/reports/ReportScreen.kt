package com.nammashalli.inventory.ui.reports

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nammashalli.inventory.ui.common.AppTopBar
import com.nammashalli.inventory.ui.common.MetricCard
import com.nammashalli.inventory.ui.common.SectionHeader
import com.nammashalli.inventory.ui.theme.*

@Composable
fun ReportScreen(
    onBack: (() -> Unit)? = null,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.pdfGenerated) {
        if (state.pdfGenerated) {
            viewModel.clearPdfGenerated()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar("Summary Report", onBack = onBack) {
                IconButton(onClick = { viewModel.loadReportData() }) {
                    Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = GreenPrimary)
                    Spacer(Modifier.height(16.dp))
                    Text("Loading report data...", color = TextSecondary)
                }
            }
        } else {
            val data = state.reportData
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // School info
                data?.let { d ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("ASSET SUMMARY REPORT", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelLarge)
                            Text(d.schoolName, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Generated: ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(d.generatedAt))}", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    // Status breakdown
                    SectionHeader("Asset Status Breakdown")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        MetricCard("Total", d.totalAssets, GreenPrimary, GreenLight, Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        MetricCard("Good", d.goodCount, StatusGreen, StatusGreenBg, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        MetricCard("Fair", d.fairCount, StatusYellow, StatusYellowBg, Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        MetricCard("Repair Needed", d.needsRepairCount, StatusRed, StatusRedBg, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        MetricCard("Lost/Damaged", d.lostCount, StatusBlack, StatusBlackBg, Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        MetricCard("Health Checks", d.totalHealthChecks, GreenPrimary, GreenLight, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        MetricCard("Pending Repairs", d.pendingRepairs, StatusRed, StatusRedBg, Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        MetricCard("Open Issues", d.openIssues, StatusYellow, StatusYellowBg, Modifier.weight(1f))
                    }

                    // Total value
                    val totalValue = d.assetDetails.sumOf { it.estimatedCost ?: 0.0 }
                    if (totalValue > 0) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GreenLight),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CurrencyRupee, null, tint = GreenPrimary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Total Asset Value", fontWeight = FontWeight.Medium, color = GreenPrimary)
                                }
                                Text("₹${String.format("%.2f", totalValue)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GreenPrimary)
                            }
                        }
                    }

                    // Category breakdown
                    if (d.assetDetails.isNotEmpty()) {
                        Spacer(Modifier.height(20.dp))
                        SectionHeader("Category Breakdown")
                        val byCategory = d.assetDetails.groupBy { it.category }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                byCategory.forEach { (cat, items) ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(cat, style = MaterialTheme.typography.bodyMedium)
                                        Text("${items.size} asset(s)", fontWeight = FontWeight.SemiBold, color = GreenPrimary)
                                    }
                                    if (cat != byCategory.keys.last()) HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Actions
                    SectionHeader("Export & Share")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { viewModel.generatePdf(context) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Export PDF")
                        }
                        if (state.pdfFile != null) {
                            OutlinedButton(
                                onClick = { viewModel.sharePdf(context) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Share")
                            }
                        }
                    }

                    // AI Insights
                    Spacer(Modifier.height(16.dp))
                    if (state.hasGroqKey) {
                        Button(
                            onClick = { viewModel.getAiInsights() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !state.isGeneratingAi
                        ) {
                            if (state.isGeneratingAi) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Analyzing...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Get AI Insights", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // AI insights result
                    state.aiInsights?.let { insights ->
                        Spacer(Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF6200EE), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("AI Insights", fontWeight = FontWeight.Bold, color = Color(0xFF6200EE))
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(insights, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    state.error?.let { err ->
                        Spacer(Modifier.height(12.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = StatusRedBg), shape = RoundedCornerShape(8.dp)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, null, tint = StatusRed, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(err, color = StatusRed, modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, null, tint = StatusRed)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
