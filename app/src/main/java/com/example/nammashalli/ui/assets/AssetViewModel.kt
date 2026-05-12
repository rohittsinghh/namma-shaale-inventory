package com.example.nammashalli.ui.assets

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammashalli.data.local.entities.AssetEntity
import com.example.nammashalli.data.repository.AssetRepository
import com.example.nammashalli.utils.ImageUtil
import com.example.nammashalli.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssetListState(
    val assets: List<AssetEntity> = emptyList(),
    val filteredAssets: List<AssetEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: String = "All",
    val isLoading: Boolean = true,
    val schoolId: Long = -1L
)

data class AssetRegisterState(
    val assetName: String = "",
    val category: String = "Lab Equipment",
    val serialNumber: String = "",
    val purchaseDate: Long? = null,
    val estimatedCost: String = "",
    val location: String = "Classroom",
    val assignedTo: String = "",
    val description: String = "",
    val photoUri: Uri? = null,
    val photoPath: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val generatedAssetId: String = ""
)

@HiltViewModel
class AssetViewModel @Inject constructor(
    private val assetRepository: AssetRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _listState = MutableStateFlow(AssetListState())
    val listState: StateFlow<AssetListState> = _listState.asStateFlow()

    private val _registerState = MutableStateFlow(AssetRegisterState())
    val registerState: StateFlow<AssetRegisterState> = _registerState.asStateFlow()

    private val _detailAsset = MutableStateFlow<AssetEntity?>(null)
    val detailAsset: StateFlow<AssetEntity?> = _detailAsset.asStateFlow()

    private var schoolId = -1L

    init {
        viewModelScope.launch {
            schoolId = sessionManager.userId.first()
            _listState.value = _listState.value.copy(schoolId = schoolId)
            loadAssets()
        }
    }

    private fun loadAssets() = viewModelScope.launch {
        assetRepository.getAllAssets(schoolId).collect { assets ->
            _listState.value = _listState.value.copy(
                assets = assets,
                filteredAssets = applyFilter(assets, _listState.value.searchQuery, _listState.value.selectedStatus),
                isLoading = false
            )
        }
    }

    fun search(query: String) {
        _listState.value = _listState.value.copy(
            searchQuery = query,
            filteredAssets = applyFilter(_listState.value.assets, query, _listState.value.selectedStatus)
        )
    }

    fun filterByStatus(status: String) {
        _listState.value = _listState.value.copy(
            selectedStatus = status,
            filteredAssets = applyFilter(_listState.value.assets, _listState.value.searchQuery, status)
        )
    }

    private fun applyFilter(assets: List<AssetEntity>, query: String, status: String): List<AssetEntity> {
        var result = assets
        if (query.isNotBlank()) {
            val q = query.lowercase()
            result = result.filter {
                it.assetName.lowercase().contains(q) || it.assetId.lowercase().contains(q) || it.category.lowercase().contains(q)
            }
        }
        if (status != "All") result = result.filter { it.currentStatus == status }
        return result
    }

    fun loadAssetDetail(assetId: Long) = viewModelScope.launch {
        _detailAsset.value = assetRepository.getById(assetId)
    }

    fun updateRegisterField(field: AssetField, value: String) {
        _registerState.value = when (field) {
            AssetField.ASSET_NAME -> _registerState.value.copy(assetName = value)
            AssetField.SERIAL_NUMBER -> _registerState.value.copy(serialNumber = value)
            AssetField.ESTIMATED_COST -> _registerState.value.copy(estimatedCost = value)
            AssetField.ASSIGNED_TO -> _registerState.value.copy(assignedTo = value)
            AssetField.DESCRIPTION -> _registerState.value.copy(description = value)
        }
    }

    fun updateCategory(category: String) { _registerState.value = _registerState.value.copy(category = category) }
    fun updateLocation(location: String) { _registerState.value = _registerState.value.copy(location = location) }
    fun updatePurchaseDate(date: Long?) { _registerState.value = _registerState.value.copy(purchaseDate = date) }
    fun setPhotoUri(uri: Uri?) { _registerState.value = _registerState.value.copy(photoUri = uri) }

    fun processPhoto(context: Context, uri: Uri) = viewModelScope.launch {
        val path = ImageUtil.compressAndSave(context, uri)
        _registerState.value = _registerState.value.copy(photoPath = path, photoUri = uri)
    }

    fun registerAsset() = viewModelScope.launch {
        val s = _registerState.value
        if (s.assetName.isBlank()) {
            _registerState.value = s.copy(error = "Asset name is required")
            return@launch
        }
        _registerState.value = s.copy(isLoading = true, error = null)
        val assetId = assetRepository.generateNextAssetId(schoolId)
        val asset = AssetEntity(
            schoolId = schoolId,
            assetId = assetId,
            assetName = s.assetName.trim(),
            category = s.category,
            serialNumber = s.serialNumber.takeIf { it.isNotBlank() },
            purchaseDate = s.purchaseDate,
            estimatedCost = s.estimatedCost.toDoubleOrNull(),
            location = s.location,
            assignedTo = s.assignedTo.takeIf { it.isNotBlank() },
            description = s.description.takeIf { it.isNotBlank() },
            photoPath = s.photoPath,
            currentStatus = "Good"
        )
        assetRepository.insertAsset(asset)
        _registerState.value = s.copy(isLoading = false, success = true, generatedAssetId = assetId)
    }

    fun resetRegisterState() {
        _registerState.value = AssetRegisterState()
    }

    fun deleteAsset(asset: AssetEntity) = viewModelScope.launch {
        ImageUtil.deleteImage(asset.photoPath)
        assetRepository.deleteAsset(asset)
    }

    fun clearError() { _registerState.value = _registerState.value.copy(error = null) }
}

enum class AssetField { ASSET_NAME, SERIAL_NUMBER, ESTIMATED_COST, ASSIGNED_TO, DESCRIPTION }
