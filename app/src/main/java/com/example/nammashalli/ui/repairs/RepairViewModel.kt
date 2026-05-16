package com.nammashalli.inventory.ui.repairs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammashalli.inventory.data.local.entities.AssetEntity
import com.nammashalli.inventory.data.local.entities.RepairRequestEntity
import com.nammashalli.inventory.data.repository.AssetRepository
import com.nammashalli.inventory.data.repository.RepairRepository
import com.nammashalli.inventory.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RepairListState(
    val repairs: List<RepairRequestEntity> = emptyList(),
    val assets: Map<Long, AssetEntity> = emptyMap(),
    val filterStatus: String = "Pending",
    val isLoading: Boolean = true
)

@HiltViewModel
class RepairViewModel @Inject constructor(
    private val repairRepository: RepairRepository,
    private val assetRepository: AssetRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(RepairListState())
    val state: StateFlow<RepairListState> = _state.asStateFlow()

    private var schoolId = -1L

    init {
        viewModelScope.launch {
            schoolId = sessionManager.userId.first()
            loadRepairs()
        }
    }

    private fun loadRepairs() = viewModelScope.launch {
        repairRepository.getAllBySchool(schoolId).collect { repairs ->
            val allAssets = assetRepository.getAllSnapshot(schoolId)
            val assetMap = allAssets.associateBy { it.id }
            val filtered = applyFilter(repairs, _state.value.filterStatus)
            _state.value = _state.value.copy(repairs = filtered, assets = assetMap, isLoading = false)
        }
    }

    private fun applyFilter(repairs: List<RepairRequestEntity>, status: String): List<RepairRequestEntity> {
        return if (status == "All") repairs else repairs.filter { it.status == status }
    }

    fun filterByStatus(status: String) {
        viewModelScope.launch {
            val allRepairs = repairRepository.getAllSnapshot(schoolId)
            _state.value = _state.value.copy(
                filterStatus = status,
                repairs = applyFilter(allRepairs, status)
            )
        }
    }

    fun markCompleted(id: Long) = viewModelScope.launch {
        repairRepository.markCompleted(id)
    }
}
