package com.nammashalli.inventory.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class ReportData(
    val schoolName: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val totalAssets: Int,
    val goodCount: Int,
    val fairCount: Int,
    val needsRepairCount: Int,
    val lostCount: Int,
    val pendingRepairs: Int,
    val openIssues: Int,
    val totalHealthChecks: Int,
    val assetDetails: List<AssetReportItem> = emptyList(),
    val repairDetails: List<RepairReportItem> = emptyList()
)

data class AssetReportItem(
    val assetId: String,
    val assetName: String,
    val category: String,
    val location: String,
    val status: String,
    val estimatedCost: Double?
)

data class RepairReportItem(
    val assetId: String,
    val assetName: String,
    val reason: String,
    val priority: String,
    val requestedAt: Long,
    val status: String
)

object PdfGenerator {

    private val titlePaint = Paint().apply {
        color = Color.parseColor("#1B5E20")
        textSize = 22f
        isFakeBoldText = true
    }
    private val headingPaint = Paint().apply {
        color = Color.parseColor("#2E7D32")
        textSize = 16f
        isFakeBoldText = true
    }
    private val bodyPaint = Paint().apply {
        color = Color.BLACK
        textSize = 12f
    }
    private val subPaint = Paint().apply {
        color = Color.GRAY
        textSize = 10f
    }
    private val dividerPaint = Paint().apply {
        color = Color.parseColor("#BDBDBD")
        strokeWidth = 1f
    }

    fun generateAssetReport(context: Context, data: ReportData): File? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var y = drawHeader(canvas, data)
            y = drawSummary(canvas, data, y)
            y = drawAssetStatusBreakdown(canvas, data, y)

            if (data.assetDetails.isNotEmpty()) {
                y = drawAssetTable(canvas, data.assetDetails, y)
            }
            document.finishPage(page)

            if (data.repairDetails.isNotEmpty()) {
                val page2Info = PdfDocument.PageInfo.Builder(595, 842, 2).create()
                val page2 = document.startPage(page2Info)
                drawRepairTable(page2.canvas, data.repairDetails)
                document.finishPage(page2)
            }

            val outFile = createOutputFile(context, "AssetReport")
            FileOutputStream(outFile).use { document.writeTo(it) }
            document.close()
            outFile
        } catch (e: Exception) {
            null
        }
    }

    private fun drawHeader(canvas: Canvas, data: ReportData): Float {
        var y = 50f
        canvas.drawText("NAMMA-SHAALE INVENTORY", 40f, y, titlePaint)
        y += 28f
        canvas.drawText(data.schoolName, 40f, y, headingPaint)
        y += 20f
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(data.generatedAt))
        canvas.drawText("Generated: $dateStr", 40f, y, subPaint)
        y += 15f
        canvas.drawLine(40f, y, 555f, y, dividerPaint)
        return y + 20f
    }

    private fun drawSummary(canvas: Canvas, data: ReportData, startY: Float): Float {
        var y = startY
        canvas.drawText("ASSET SUMMARY", 40f, y, headingPaint)
        y += 22f

        val totalValue = data.assetDetails.sumOf { it.estimatedCost ?: 0.0 }
        drawRow(canvas, "Total Assets", data.totalAssets.toString(), y)
        y += 18f
        drawRow(canvas, "Total Estimated Value", "₹${String.format("%.2f", totalValue)}", y)
        y += 18f
        drawRow(canvas, "Pending Repairs", data.pendingRepairs.toString(), y)
        y += 18f
        drawRow(canvas, "Open Issues", data.openIssues.toString(), y)
        y += 18f
        drawRow(canvas, "Health Checks Done", data.totalHealthChecks.toString(), y)
        y += 22f
        return y
    }

    private fun drawAssetStatusBreakdown(canvas: Canvas, data: ReportData, startY: Float): Float {
        var y = startY
        canvas.drawLine(40f, y, 555f, y, dividerPaint)
        y += 16f
        canvas.drawText("STATUS BREAKDOWN", 40f, y, headingPaint)
        y += 22f

        drawColorRow(canvas, "Good (Green)", data.goodCount.toString(), Color.parseColor("#2E7D32"), y)
        y += 20f
        drawColorRow(canvas, "Fair (Yellow)", data.fairCount.toString(), Color.parseColor("#F57F17"), y)
        y += 20f
        drawColorRow(canvas, "Needs Repair (Red)", data.needsRepairCount.toString(), Color.parseColor("#B71C1C"), y)
        y += 20f
        drawColorRow(canvas, "Lost/Damaged (Black)", data.lostCount.toString(), Color.BLACK, y)
        y += 28f
        return y
    }

    private fun drawAssetTable(canvas: Canvas, assets: List<AssetReportItem>, startY: Float): Float {
        var y = startY
        if (y > 720f) return y
        canvas.drawLine(40f, y, 555f, y, dividerPaint)
        y += 16f
        canvas.drawText("ASSET DETAILS", 40f, y, headingPaint)
        y += 20f

        val headerPaint = Paint(bodyPaint).apply { isFakeBoldText = true }
        canvas.drawText("Asset ID", 40f, y, headerPaint)
        canvas.drawText("Name", 110f, y, headerPaint)
        canvas.drawText("Category", 270f, y, headerPaint)
        canvas.drawText("Status", 410f, y, headerPaint)
        y += 6f
        canvas.drawLine(40f, y, 555f, y, dividerPaint)
        y += 14f

        for (asset in assets) {
            if (y > 800f) break
            canvas.drawText(asset.assetId, 40f, y, bodyPaint)
            canvas.drawText(asset.assetName.take(18), 110f, y, bodyPaint)
            canvas.drawText(asset.category.take(14), 270f, y, bodyPaint)
            val statusColor = when (asset.status) {
                "Good" -> Color.parseColor("#2E7D32")
                "Fair" -> Color.parseColor("#F57F17")
                "NeedsRepair" -> Color.parseColor("#B71C1C")
                else -> Color.BLACK
            }
            Paint(bodyPaint).apply { color = statusColor }.also {
                canvas.drawText(asset.status, 410f, y, it)
            }
            y += 18f
        }
        return y
    }

    private fun drawRepairTable(canvas: Canvas, repairs: List<RepairReportItem>) {
        var y = 50f
        canvas.drawText("REPAIR REQUESTS", 40f, y, titlePaint)
        y += 30f

        val headerPaint = Paint(bodyPaint).apply { isFakeBoldText = true }
        canvas.drawText("Asset ID", 40f, y, headerPaint)
        canvas.drawText("Asset Name", 110f, y, headerPaint)
        canvas.drawText("Priority", 270f, y, headerPaint)
        canvas.drawText("Status", 360f, y, headerPaint)
        canvas.drawText("Date", 440f, y, headerPaint)
        y += 6f
        canvas.drawLine(40f, y, 555f, y, dividerPaint)
        y += 14f

        val fmt = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        for (r in repairs) {
            if (y > 800f) break
            canvas.drawText(r.assetId, 40f, y, bodyPaint)
            canvas.drawText(r.assetName.take(18), 110f, y, bodyPaint)
            canvas.drawText(r.priority, 270f, y, bodyPaint)
            canvas.drawText(r.status, 360f, y, bodyPaint)
            canvas.drawText(fmt.format(Date(r.requestedAt)), 440f, y, bodyPaint)
            y += 18f
        }
    }

    private fun drawRow(canvas: Canvas, label: String, value: String, y: Float) {
        canvas.drawText(label, 40f, y, bodyPaint)
        canvas.drawText(value, 300f, y, bodyPaint)
    }

    private fun drawColorRow(canvas: Canvas, label: String, value: String, color: Int, y: Float) {
        Paint(bodyPaint).apply { this.color = color }.also { canvas.drawText("●", 40f, y, it) }
        canvas.drawText(label, 60f, y, bodyPaint)
        canvas.drawText(value, 300f, y, bodyPaint)
    }

    private fun createOutputFile(context: Context, prefix: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dir = File(context.filesDir, "reports").apply { mkdirs() }
        return File(dir, "${prefix}_$timestamp.pdf")
    }
}
