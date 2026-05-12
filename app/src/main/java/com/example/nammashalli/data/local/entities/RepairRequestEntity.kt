package com.example.nammashalli.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repair_requests")
data class RepairRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetId: Long,
    val schoolId: Long,
    val reason: String,
    val priority: String = "Medium",
    val requestedAt: Long = System.currentTimeMillis(),
    val requestedBy: String,
    val status: String = "Pending",
    val completedAt: Long? = null,
    val notes: String? = null
)
