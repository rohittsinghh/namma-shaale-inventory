package com.example.nammashalli.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammashalli.data.repository.*
import com.example.nammashalli.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val userName: String = "",
    val schoolName: String = "",
    val role: String = "",
    val totalAssets: Int = 0,
    val goodCount: Int = 0,
    val fairCount: Int = 0,
    val needsRepairCount: Int = 0,
    val lostCount: Int = 0,
    val pendingRepairs: Int = 0,
    val openIssues: Int = 0,
    val totalHealthChecks: Int = 0,
    val isLoading: Boolean = true,
    val userId: Long = -1L
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val assetRepository: AssetRepository,
    private val repairRepository: RepairRepository,
    private val issueRepository: IssueRepository,
    private val healthCheckRepository: HealthCheckRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true)
        val userId = sessionManager.userId.first()
        val userName = sessionManager.userName.first()
        val schoolName = sessionManager.schoolName.first()
        val role = sessionManager.userRole.first()

        // Use userId as schoolId for simplicity (1:1 user-school mapping)
        val schoolId = userId
        val counts = assetRepository.getStatusCounts(schoolId)
        val pendingRepairs = repairRepository.countPending(schoolId)
        val openIssues = issueRepository.countOpen(schoolId)
        val totalChecks = healthCheckRepository.countBySchool(schoolId)

        _state.value = DashboardState(
            userName = userName,
            schoolName = schoolName,
            role = role,
            totalAssets = counts.total,
            goodCount = counts.good,
            fairCount = counts.fair,
            needsRepairCount = counts.needsRepair,
            lostCount = counts.lost,
            pendingRepairs = pendingRepairs,
            openIssues = openIssues,
            totalHealthChecks = totalChecks,
            isLoading = false,
            userId = userId
        )
    }

    fun logout() = viewModelScope.launch {
        sessionManager.clearSession()
    }
}
