package com.example.nammashalli.ui.issues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammashalli.data.local.entities.AssetEntity
import com.example.nammashalli.data.local.entities.IssueLogEntity
import com.example.nammashalli.data.repository.AssetRepository
import com.example.nammashalli.data.repository.IssueRepository
import com.example.nammashalli.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IssueLogFormState(
    val assets: List<AssetEntity> = emptyList(),
    val selectedAsset: AssetEntity? = null,
    val issueType: String = "Broken",
    val description: String = "",
    val issueDate: Long = System.currentTimeMillis(),
    val locationFound: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

data class IssueListState(
    val issues: List<IssueLogEntity> = emptyList(),
    val assets: Map<Long, AssetEntity> = emptyMap(),
    val filterStatus: String = "All",
    val isLoading: Boolean = true
)

@HiltViewModel
class IssueViewModel @Inject constructor(
    private val issueRepository: IssueRepository,
    private val assetRepository: AssetRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _formState = MutableStateFlow(IssueLogFormState())
    val formState: StateFlow<IssueLogFormState> = _formState.asStateFlow()

    private val _listState = MutableStateFlow(IssueListState())
    val listState: StateFlow<IssueListState> = _listState.asStateFlow()

    private var schoolId = -1L
    private var userName = ""

    init {
        viewModelScope.launch {
            schoolId = sessionManager.userId.first()
            userName = sessionManager.userName.first()
            val assets = assetRepository.getAllSnapshot(schoolId)
            _formState.value = _formState.value.copy(assets = assets)
            loadIssues()
        }
    }

    private fun loadIssues() = viewModelScope.launch {
        issueRepository.getAllBySchool(schoolId).collect { issues ->
            val allAssets = assetRepository.getAllSnapshot(schoolId)
            val assetMap = allAssets.associateBy { it.id }
            _listState.value = _listState.value.copy(
                issues = applyFilter(issues, _listState.value.filterStatus),
                assets = assetMap,
                isLoading = false
            )
        }
    }

    private fun applyFilter(issues: List<IssueLogEntity>, filter: String): List<IssueLogEntity> {
        return if (filter == "All") issues else issues.filter { it.status == filter }
    }

    fun filterByStatus(status: String) {
        _listState.value = _listState.value.copy(
            filterStatus = status,
            issues = applyFilter(_listState.value.issues, status)
        )
    }

    fun selectAsset(asset: AssetEntity) { _formState.value = _formState.value.copy(selectedAsset = asset) }
    fun setIssueType(type: String) { _formState.value = _formState.value.copy(issueType = type) }
    fun setDescription(desc: String) { _formState.value = _formState.value.copy(description = desc) }
    fun setIssueDate(date: Long) { _formState.value = _formState.value.copy(issueDate = date) }
    fun setLocation(loc: String) { _formState.value = _formState.value.copy(locationFound = loc) }

    fun submitIssue() = viewModelScope.launch {
        val s = _formState.value
        if (s.selectedAsset == null) { _formState.value = s.copy(error = "Please select an asset"); return@launch }
        if (s.description.isBlank()) { _formState.value = s.copy(error = "Please describe the issue"); return@launch }
        _formState.value = s.copy(isLoading = true, error = null)
        val issue = IssueLogEntity(
            assetId = s.selectedAsset.id,
            schoolId = schoolId,
            issueType = s.issueType,
            description = s.description.trim(),
            issueDate = s.issueDate,
            reportedBy = userName,
            location = s.locationFound.takeIf { it.isNotBlank() }
        )
        issueRepository.insert(issue)
        _formState.value = s.copy(isLoading = false, success = true)
    }

    fun resolveIssue(id: Long) = viewModelScope.launch { issueRepository.resolve(id) }
    fun clearError() { _formState.value = _formState.value.copy(error = null) }
    fun resetForm() { _formState.value = IssueLogFormState(assets = _formState.value.assets) }
}
