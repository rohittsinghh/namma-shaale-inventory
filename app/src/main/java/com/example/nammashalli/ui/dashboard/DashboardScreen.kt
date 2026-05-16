package com.nammashalli.inventory.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nammashalli.inventory.ui.common.MetricCard
import com.nammashalli.inventory.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAssetList: () -> Unit,
    onNavigateToAssetRegister: () -> Unit,
    onNavigateToHealthCheck: () -> Unit,
    onNavigateToRepairs: () -> Unit,
    onNavigateToIssues: () -> Unit,
    onNavigateToReport: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = { viewModel.logout(); onLogout() }) {
                    Text("Sign Out", color = StatusRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Namma-Shaale Inventory", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToReport) {
                        Icon(Icons.Default.Assessment, "Reports", tint = Color.White)
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAssetRegister,
                containerColor = GreenPrimary
            ) {
                Icon(Icons.Default.Add, "Add Asset", tint = Color.White)
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(SurfaceVariant)
                    .verticalScroll(rememberScrollState())
            ) {
                // Welcome header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenPrimary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Welcome, ${state.userName.split(" ").first()}!", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Text(state.schoolName, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
                        Text(state.role, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                    // Asset overview card
                    Text("Asset Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                MetricCard("Total", state.totalAssets, GreenPrimary, GreenLight, Modifier.weight(1f))
                                Spacer(Modifier.width(8.dp))
                                MetricCard("Good", state.goodCount, StatusGreen, StatusGreenBg, Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                MetricCard("Fair", state.fairCount, StatusYellow, StatusYellowBg, Modifier.weight(1f))
                                Spacer(Modifier.width(8.dp))
                                MetricCard("Repair Needed", state.needsRepairCount, StatusRed, StatusRedBg, Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(8.dp))
                            MetricCard("Lost/Damaged", state.lostCount, StatusBlack, StatusBlackBg, Modifier.fillMaxWidth())
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Alert strip
                    if (state.needsRepairCount > 0 || state.lostCount > 0 || state.pendingRepairs > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StatusRedBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = StatusRed, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Attention Required", fontWeight = FontWeight.Bold, color = StatusRed)
                                    if (state.needsRepairCount > 0) Text("${state.needsRepairCount} assets need repair", style = MaterialTheme.typography.bodySmall, color = StatusRed)
                                    if (state.pendingRepairs > 0) Text("${state.pendingRepairs} pending repair requests", style = MaterialTheme.typography.bodySmall, color = StatusRed)
                                    if (state.openIssues > 0) Text("${state.openIssues} open issues", style = MaterialTheme.typography.bodySmall, color = StatusRed)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // Quick actions
                    Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard("View Assets", Icons.Default.Inventory, GreenLight, GreenPrimary, onNavigateToAssetList, Modifier.weight(1f))
                        Spacer(Modifier.width(12.dp))
                        QuickActionCard("Health Check", Icons.Default.HealthAndSafety, StatusGreenBg, StatusGreen, onNavigateToHealthCheck, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard("Repairs", Icons.Default.Build, StatusRedBg, StatusRed, onNavigateToRepairs, Modifier.weight(1f))
                        Spacer(Modifier.width(12.dp))
                        QuickActionCard("Log Issue", Icons.Default.ReportProblem, StatusYellowBg, StatusYellow, onNavigateToIssues, Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(16.dp))

                    // Stats strip
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Health Checks", state.totalHealthChecks.toString(), Icons.Default.CheckCircle)
                            VerticalDivider(modifier = Modifier.height(40.dp))
                            StatItem("Pending Repairs", state.pendingRepairs.toString(), Icons.Default.Build)
                            VerticalDivider(modifier = Modifier.height(40.dp))
                            StatItem("Open Issues", state.openIssues.toString(), Icons.Default.Warning)
                        }
                    }
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(bgColor, RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = StatusBlack)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GreenPrimary)
        Text(label, fontSize = 10.sp, color = TextSecondary)
    }
}
