package com.nammashalli.inventory.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nammashalli.inventory.data.local.entities.AssetEntity
import com.nammashalli.inventory.ui.common.*
import com.nammashalli.inventory.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetListScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: AssetViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()
    val statusFilters = listOf("All", "Good", "Fair", "NeedsRepair", "Lost")

    Scaffold(
        topBar = {
            AppTopBar("Asset Inventory", onBack = onBack) {
                IconButton(onClick = onNavigateToRegister) {
                    Icon(Icons.Default.Add, "Add", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(SurfaceVariant)) {
            // Search
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.search(it) },
                placeholder = { Text("Search by name, ID, or category...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.search("") }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            // Status filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(statusFilters) { filter ->
                    val isSelected = state.selectedStatus == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.filterByStatus(filter) },
                        label = { Text(filter.replace("NeedsRepair", "Repair")) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else if (state.filteredAssets.isEmpty()) {
                EmptyState(
                    message = if (state.searchQuery.isNotBlank()) "No assets match your search" else "No assets registered yet",
                    icon = Icons.Default.Inventory,
                    actionLabel = "Register Asset",
                    onAction = onNavigateToRegister
                )
            } else {
                Text(
                    "${state.filteredAssets.size} asset(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.filteredAssets, key = { it.id }) { asset ->
                        AssetCard(asset = asset, onClick = { onNavigateToDetails(asset.id) })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun AssetCard(asset: AssetEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val status = AssetStatus.fromString(asset.currentStatus)
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(status.bgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(status.emoji, fontSize = 22.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(asset.assetName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Text(asset.assetId, style = MaterialTheme.typography.bodySmall, color = GreenPrimary, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(asset.category, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("•", color = TextSecondary)
                    Text(asset.location, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                asset.estimatedCost?.let {
                    Text("₹${String.format("%.0f", it)}", style = MaterialTheme.typography.labelSmall, color = GreenPrimary, fontWeight = FontWeight.Medium)
                }
            }
            StatusBadge(asset.currentStatus)
        }
    }
}
