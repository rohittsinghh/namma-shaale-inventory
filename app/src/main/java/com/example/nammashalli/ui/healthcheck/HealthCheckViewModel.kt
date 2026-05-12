package com.example.nammashalli.ui.healthcheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammashalli.data.local.entities.AssetEntity
import com.example.nammashalli.data.local.entities.HealthCheckEntity
import com.example.nammashalli.data.local.entities.RepairRequestEntity
import com.example.nammashalli.data.repository.*
import com.example.nammashalli.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HealthCheckItem(
    val asset: AssetEntity,
    val status: String = asset.currentStatus,
    val notes: String = ""
)

data class HealthCheckState(
    val assets: List<AssetEntity> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val checkItems: List<HealthCheckItem> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val submitted: Boolean = false,
    val startTimeMs: Long = 0L,
    val elapsedMs: Long = 0L,
    val userName: String = ""
)

@HiltViewModel
class HealthCheckViewModel @Inject constructor(
    private val assetRepository: AssetRepository,
    private val healthCheckRepository: HealthCheckRepository,
    private val repairRepository: RepairRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(HealthCheckState())
    val state: StateFlow<HealthCheckState> = _state.asStateFlow()

    private var schoolId = -1L
    private var userName = ""

    init {
        viewModelScope.launch {
            schoolId = sessionManager.userId.first()
            userName = sessionManager.userName.first()
            val assets = assetRepository.getAllSnapshot(schoolId)
            _state.value = _state.value.copy(assets = assets, isLoading = false, userName = userName)
        }
    }

    fun toggleSelect(assetId: Long) {
        val current = _state.value.selectedIds.toMutableSet()
        if (current.contains(assetId)) current.remove(assetId) else current.add(assetId)
        _state.value = _state.value.copy(selectedIds = current)
    }

    fun selectAll() {
        _state.value = _state.value.copy(selectedIds = _state.value.assets.map { it.id }.toSet())
    }

    fun selectNone() {
        _state.value = _state.value.copy(selectedIds = emptySet())
    }

    fun startHealthCheck() {
        val selected = _state.value.assets.filter { _state.value.selectedIds.contains(it.id) }
        if (selected.isEmpty()) return
        val items = selected.map { HealthCheckItem(asset = it) }
        _state.value = _state.value.copy(
            checkItems = items,
            currentIndex = 0,
            startTimeMs = System.currentTimeMillis()
        )
    }

    fun setStatus(status: String) {
        val items = _state.value.checkItems.toMutableList()
        val idx = _state.value.currentIndex
        if (idx < items.size) {
            items[idx] = items[idx].copy(status = status)
            _state.value = _state.value.copy(checkItems = items)
        }
    }

    fun setNotes(notes: String) {
        val items = _state.value.checkItems.toMutableList()
        val idx = _state.value.currentIndex
        if (idx < items.size) {
            items[idx] = items[idx].copy(notes = notes)
            _state.value = _state.value.copy(checkItems = items)
        }
    }

    fun next() {
        val nextIdx = _state.value.currentIndex + 1
        _state.value = _state.value.copy(
            currentIndex = nextIdx,
            elapsedMs = System.currentTimeMillis() - _state.value.startTimeMs
        )
    }

    fun previous() {
        val prevIdx = (_state.value.currentIndex - 1).coerceAtLeast(0)
        _state.value = _state.value.copy(currentIndex = prevIdx)
    }

    fun submitHealthCheck() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true)
        val now = System.currentTimeMillis()
        val elapsed = now - _state.value.startTimeMs
        val checks = _state.value.checkItems.map { item ->
            HealthCheckEntity(
                assetId = item.asset.id,
                schoolId = schoolId,
                status = item.status,
                notes = item.notes.takeIf { it.isNotBlank() },
                checkedBy = userName
            )
        }
        healthCheckRepository.insertAll(checks)

        // Update asset statuses and create repair requests for red items
        val repairRequests = mutableListOf<RepairRequestEntity>()
        _state.value.checkItems.forEach { item ->
            assetRepository.updateStatus(item.asset.id, item.status)
            if (item.status == "NeedsRepair") {
                repairRequests.add(
                    RepairRequestEntity(
                        assetId = item.asset.id,
                        schoolId = schoolId,
                        reason = "Flagged during health check: ${item.notes.ifBlank { "Needs repair" }}",
                        priority = "High",
                        requestedBy = userName
                    )
                )
            }
        }
        if (repairRequests.isNotEmpty()) repairRepository.insertAll(repairRequests)

        _state.value = _state.value.copy(isLoading = false, submitted = true, elapsedMs = elapsed)
    }
}
