package com.example.nammashalli.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assets",
    indices = [Index(value = ["assetId"], unique = true)]
)
data class AssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long,
    val assetId: String,
    val assetName: String,
    val category: String,
    val serialNumber: String? = null,
    val purchaseDate: Long? = null,
    val estimatedCost: Double? = null,
    val location: String,
    val assignedTo: String? = null,
    val description: String? = null,
    val photoPath: String? = null,
    val currentStatus: String = "Good",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
