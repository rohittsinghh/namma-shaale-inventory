package com.nammashalli.inventory.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_checks")
data class HealthCheckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetId: Long,
    val schoolId: Long,
    val status: String,
    val notes: String? = null,
    val checkedBy: String,
    val checkedAt: Long = System.currentTimeMillis()
)
