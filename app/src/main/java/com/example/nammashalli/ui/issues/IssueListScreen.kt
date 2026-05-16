package com.nammashalli.inventory.ui.issues

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
import com.nammashalli.inventory.ui.common.AppTopBar
import com.nammashalli.inventory.ui.common.EmptyState
import com.nammashalli.inventory.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IssueListScreen(
    onBack: () -> Unit,
    onNavigateToLog: () -> Unit,
    viewModel: IssueViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()
    val filters = listOf("All", "Open", "Resolved")

    Scaffold(
        topBar = {
            AppTopBar("Issue Logs", onBack = onBack) {
                IconButton(onClick = onNavigateToLog) {
                    Icon(Icons.Default.Add, "Log Issue", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            } else if (state.issues.isEmpty()) {
                EmptyState("No issues logged", Icons.Default.CheckCircle, "Log Issue", onNavigateToLog)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.issues, key = { it.id }) { issue ->
                        val asset = state.assets[issue.assetId]
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
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (issue.status == "Open") StatusRedBg else StatusGreenBg
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            issue.status,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (issue.status == "Open") StatusRed else StatusGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = StatusYellowBg),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            issue.issueType,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StatusYellow
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(issue.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(issue.reportedAt)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                    if (issue.status == "Open") {
                                        TextButton(
                                            onClick = { viewModel.resolveIssue(issue.id) },
                                            colors = ButtonDefaults.textButtonColors(contentColor = GreenPrimary)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Mark Resolved", style = MaterialTheme.typography.labelMedium)
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
