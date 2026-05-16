package com.nammashalli.inventory.ui.assets

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nammashalli.inventory.ui.common.*
import com.nammashalli.inventory.ui.theme.*
import com.nammashalli.inventory.utils.ImageUtil
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetRegisterScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AssetViewModel = hiltViewModel()
) {
    val state by viewModel.registerState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showImageOptions by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    var readyToLaunchCamera by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.processPhoto(context, it) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraImageUri?.let { viewModel.processPhoto(context, it) }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) readyToLaunchCamera = true
    }

    // Launch camera on next frame after permission granted — avoids chaining two startActivityForResult calls
    LaunchedEffect(readyToLaunchCamera) {
        if (readyToLaunchCamera) {
            readyToLaunchCamera = false
            cameraImageUri?.let { cameraLauncher.launch(it) }
        }
    }

    fun launchCamera() {
        try {
            val file = ImageUtil.createImageFile(context)
            cameraImageUri = ImageUtil.getUriForFile(context, file)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraLauncher.launch(cameraImageUri!!)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } catch (_: Exception) {}
    }

    LaunchedEffect(state.success) {
        if (state.success) onSuccess()
    }

    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            title = { Text("Add Photo") },
            text = {
                Column {
                    TextButton(
                        onClick = { showImageOptions = false; launchCamera() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Camera, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Take Photo")
                    }
                    TextButton(
                        onClick = { showImageOptions = false; galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Choose from Gallery")
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = { AppTopBar("Register New Asset", onBack = onNavigateBack) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                SectionHeader("Basic Information")

                OutlinedTextField(
                    value = state.assetName,
                    onValueChange = { viewModel.updateRegisterField(AssetField.ASSET_NAME, it) },
                    label = { Text("Asset Name *") },
                    leadingIcon = { Icon(Icons.Default.Inventory, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = state.error?.contains("name") == true
                )
                Spacer(Modifier.height(12.dp))

                // Category
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                    OutlinedTextField(
                        value = state.category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category *") },
                        leadingIcon = { Icon(Icons.Default.Category, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        AssetCategory.all().forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { viewModel.updateCategory(cat); categoryExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.serialNumber,
                    onValueChange = { viewModel.updateRegisterField(AssetField.SERIAL_NUMBER, it) },
                    label = { Text("Serial Number / Asset ID") },
                    leadingIcon = { Icon(Icons.Default.Numbers, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Optional") }
                )
                Spacer(Modifier.height(12.dp))

                // Purchase date picker
                val calendar = Calendar.getInstance()
                state.purchaseDate?.let { calendar.timeInMillis = it }
                val dateText = state.purchaseDate?.let {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
                } ?: "Select purchase date"

                Box {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Purchase Date") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        Calendar.getInstance().also { cal ->
                                            cal.set(y, m, d)
                                            viewModel.updatePurchaseDate(cal.timeInMillis)
                                        }
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                    )
                }
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.estimatedCost,
                    onValueChange = { viewModel.updateRegisterField(AssetField.ESTIMATED_COST, it) },
                    label = { Text("Estimated Cost (₹)") },
                    leadingIcon = { Text("₹", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold, color = GreenPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))

                // Location
                ExposedDropdownMenuBox(expanded = locationExpanded, onExpandedChange = { locationExpanded = it }) {
                    OutlinedTextField(
                        value = state.location,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Location *") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(locationExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = locationExpanded, onDismissRequest = { locationExpanded = false }) {
                        AssetLocation.all().forEach { loc ->
                            DropdownMenuItem(text = { Text(loc) }, onClick = { viewModel.updateLocation(loc); locationExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.assignedTo,
                    onValueChange = { viewModel.updateRegisterField(AssetField.ASSIGNED_TO, it) },
                    label = { Text("Assigned To") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Optional - teacher/staff name") }
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.description,
                    onValueChange = { viewModel.updateRegisterField(AssetField.DESCRIPTION, it) },
                    label = { Text("Description") },
                    leadingIcon = { Icon(Icons.Default.Notes, null) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    placeholder = { Text("Optional description...") },
                    maxLines = 4
                )
                Spacer(Modifier.height(20.dp))

                SectionHeader("Asset Photo")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, if (state.photoUri != null) GreenPrimary else DividerColor, RoundedCornerShape(12.dp))
                        .background(SurfaceVariant)
                        .clickable { showImageOptions = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (state.photoUri != null) {
                        AsyncImage(
                            model = state.photoUri,
                            contentDescription = "Asset photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                        )
                        Box(
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                .background(GreenPrimary, RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Tap to add photo", color = TextSecondary)
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

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
                    onClick = { viewModel.registerAsset() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Register Asset", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
