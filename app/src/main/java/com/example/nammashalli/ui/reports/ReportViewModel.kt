package com.nammashalli.inventory.ui.reports

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammashalli.inventory.data.repository.*
import com.nammashalli.inventory.network.GroqApiService
import com.nammashalli.inventory.network.GroqMessage
import com.nammashalli.inventory.network.GroqRequest
import com.nammashalli.inventory.utils.EncryptionUtil
import com.nammashalli.inventory.utils.PdfGenerator
import com.nammashalli.inventory.utils.ReportData
import com.nammashalli.inventory.utils.AssetReportItem
import com.nammashalli.inventory.utils.RepairReportItem
import com.nammashalli.inventory.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ReportScreenState(
    val isLoading: Boolean = true,
    val reportData: ReportData? = null,
    val pdfFile: File? = null,
    val aiInsights: String? = null,
    val isGeneratingAi: Boolean = false,
    val error: String? = null,
    val pdfGenerated: Boolean = false,
    val hasGroqKey: Boolean = false
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val assetRepository: AssetRepository,
    private val healthCheckRepository: HealthCheckRepository,
    private val issueRepository: IssueRepository,
    private val repairRepository: RepairRepository,
    private val groqApiService: GroqApiService
) : ViewModel() {

    private val _state = MutableStateFlow(ReportScreenState())
    val state: StateFlow<ReportScreenState> = _state.asStateFlow()

    private var schoolId = -1L

    init {
        viewModelScope.launch {
            schoolId = sessionManager.userId.first()
            val groqKey = sessionManager.groqApiKey.first()
            _state.value = _state.value.copy(hasGroqKey = !groqKey.isNullOrBlank())
            loadReportData()
        }
    }

    fun loadReportData() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            val schoolName = sessionManager.schoolName.first()
            val counts = assetRepository.getStatusCounts(schoolId)
            val allAssets = assetRepository.getAllSnapshot(schoolId)
            val allRepairs = repairRepository.getAllSnapshot(schoolId)
            val totalChecks = healthCheckRepository.countBySchool(schoolId)
            val openIssues = issueRepository.countOpen(schoolId)
            val pendingRepairs = repairRepository.countPending(schoolId)

            val assetItems = allAssets.map { a ->
                AssetReportItem(
                    assetId = a.assetId,
                    assetName = a.assetName,
                    category = a.category,
                    location = a.location,
                    status = a.currentStatus,
                    estimatedCost = a.estimatedCost
                )
            }

            val repairItems = allRepairs.map { r ->
                val asset = allAssets.find { it.id == r.assetId }
                RepairReportItem(
                    assetId = asset?.assetId ?: "N/A",
                    assetName = asset?.assetName ?: "Unknown",
                    reason = r.reason,
                    priority = r.priority,
                    requestedAt = r.requestedAt,
                    status = r.status
                )
            }

            val reportData = ReportData(
                schoolName = schoolName,
                totalAssets = counts.total,
                goodCount = counts.good,
                fairCount = counts.fair,
                needsRepairCount = counts.needsRepair,
                lostCount = counts.lost,
                pendingRepairs = pendingRepairs,
                openIssues = openIssues,
                totalHealthChecks = totalChecks,
                assetDetails = assetItems,
                repairDetails = repairItems
            )
            _state.value = _state.value.copy(isLoading = false, reportData = reportData)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = "Failed to load report: ${e.message}")
        }
    }

    fun generatePdf(context: Context) = viewModelScope.launch {
        val data = _state.value.reportData ?: return@launch
        _state.value = _state.value.copy(isLoading = true)
        val file = PdfGenerator.generateAssetReport(context, data)
        _state.value = _state.value.copy(isLoading = false, pdfFile = file, pdfGenerated = file != null)
    }

    fun sharePdf(context: Context) {
        val file = _state.value.pdfFile ?: return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Namma-Shaale Asset Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }

    fun getAiInsights() = viewModelScope.launch {
        val data = _state.value.reportData ?: return@launch
        val encryptedKey = sessionManager.groqApiKey.first()
        if (encryptedKey.isNullOrBlank()) {
            _state.value = _state.value.copy(error = "No Groq API key configured. Add it in your profile.")
            return@launch
        }
        val groqKey = EncryptionUtil.decryptApiKey(encryptedKey)
        if (groqKey.isBlank()) {
            _state.value = _state.value.copy(error = "Could not read API key. Please update it in your profile.")
            return@launch
        }
        _state.value = _state.value.copy(isGeneratingAi = true, error = null)
        try {
            val prompt = """
                Analyze this school asset data and provide 3-5 practical maintenance recommendations:
                - Total assets: ${data.totalAssets}
                - Good condition: ${data.goodCount}
                - Fair condition: ${data.fairCount}
                - Needs repair: ${data.needsRepairCount}
                - Lost/Damaged: ${data.lostCount}
                - Pending repair requests: ${data.pendingRepairs}
                - Open issues: ${data.openIssues}
                - Total health checks done: ${data.totalHealthChecks}

                Provide concise, actionable recommendations for a school teacher or SDMC member.
            """.trimIndent()

            val response = groqApiService.getChatCompletion(
                authorization = "Bearer $groqKey",
                request = GroqRequest(messages = listOf(GroqMessage("user", prompt)))
            )
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                _state.value = _state.value.copy(isGeneratingAi = false, error = "AI request failed: $errorBody")
                return@launch
            }
            val insights = response.body()?.choices?.firstOrNull()?.message?.content
                ?: "No insights available."
            _state.value = _state.value.copy(isGeneratingAi = false, aiInsights = insights)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isGeneratingAi = false, error = "AI insights failed: ${e.message}")
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
    fun clearPdfGenerated() { _state.value = _state.value.copy(pdfGenerated = false) }
}
