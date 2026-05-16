package com.nammashalli.inventory.ui.issues

import android.app.DatePickerDialog
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
import com.nammashalli.inventory.ui.common.*
import com.nammashalli.inventory.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueLogScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: IssueViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var assetExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.success) {
        if (state.success) { viewModel.resetForm(); onSuccess() }
    }

    Scaffold(
        topBar = { AppTopBar("Log Issue", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SectionHeader("Issue Details")

            // Asset selector
            ExposedDropdownMenuBox(expanded = assetExpanded, onExpandedChange = { assetExpanded = it }) {
                OutlinedTextField(
                    value = state.selectedAsset?.assetName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Asset *") },
                    leadingIcon = { Icon(Icons.Default.Inventory, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(assetExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    placeholder = { Text("Choose an asset") }
                )
                ExposedDropdownMenu(expanded = assetExpanded, onDismissRequest = { assetExpanded = false }) {
                    state.assets.forEach { asset ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(asset.assetName, fontWeight = FontWeight.Medium)
                                    Text("${asset.assetId} • ${asset.location}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            },
                            onClick = { viewModel.selectAsset(asset); assetExpanded = false }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Issue type
            Text("Issue Type *", style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.padding(bottom = 8.dp))
            IssueType.all().chunked(3).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { type ->
                        FilterChip(
                            selected = state.issueType == type,
                            onClick = { viewModel.setIssueType(type) },
                            label = { Text(type, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Issue Description *") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5,
                placeholder = { Text("Describe the issue in detail...") }
            )
            Spacer(Modifier.height(12.dp))

            // Date picker
            val dateText = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(state.issueDate))
            val cal = Calendar.getInstance().apply { timeInMillis = state.issueDate }
            OutlinedTextField(
                value = dateText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date of Issue") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        DatePickerDialog(context, { _, y, m, d ->
                            Calendar.getInstance().also { c -> c.set(y, m, d); viewModel.setIssueDate(c.timeInMillis) }
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    }) { Icon(Icons.Default.Edit, null) }
                }
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.locationFound,
                onValueChange = { viewModel.setLocation(it) },
                label = { Text("Location Found/Lost") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Where was the issue discovered?") }
            )
            Spacer(Modifier.height(24.dp))

            state.error?.let {
                Card(colors = CardDefaults.cardColors(containerColor = StatusRedBg), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = StatusRed, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(it, color = StatusRed)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = { viewModel.submitIssue() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.ReportProblem, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Issue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
