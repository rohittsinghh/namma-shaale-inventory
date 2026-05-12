package com.example.nammashalli.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issue_logs")
data class IssueLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetId: Long,
    val schoolId: Long,
    val issueType: String,
    val description: String,
    val issueDate: Long,
    val reportedBy: String,
    val reportedAt: Long = System.currentTimeMillis(),
    val location: String? = null,
    val photoPath: String? = null,
    val status: String = "Open"
)
