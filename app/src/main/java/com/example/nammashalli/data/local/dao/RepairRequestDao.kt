package com.nammashalli.inventory.data.local.dao

import androidx.room.*
import com.nammashalli.inventory.data.local.entities.RepairRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: RepairRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(requests: List<RepairRequestEntity>)

    @Update
    suspend fun update(request: RepairRequestEntity)

    @Query("SELECT * FROM repair_requests WHERE schoolId = :schoolId ORDER BY requestedAt DESC")
    fun getAllBySchool(schoolId: Long): Flow<List<RepairRequestEntity>>

    @Query("SELECT * FROM repair_requests WHERE schoolId = :schoolId AND status = 'Pending' ORDER BY CASE priority WHEN 'High' THEN 1 WHEN 'Medium' THEN 2 ELSE 3 END")
    fun getPending(schoolId: Long): Flow<List<RepairRequestEntity>>

    @Query("SELECT * FROM repair_requests WHERE schoolId = :schoolId AND status = 'Completed' ORDER BY completedAt DESC")
    fun getCompleted(schoolId: Long): Flow<List<RepairRequestEntity>>

    @Query("SELECT * FROM repair_requests WHERE assetId = :assetId ORDER BY requestedAt DESC")
    fun getForAsset(assetId: Long): Flow<List<RepairRequestEntity>>

    @Query("SELECT COUNT(*) FROM repair_requests WHERE schoolId = :schoolId AND status = 'Pending'")
    suspend fun countPending(schoolId: Long): Int

    @Query("UPDATE repair_requests SET status = 'Completed', completedAt = :now WHERE id = :id")
    suspend fun markCompleted(id: Long, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM repair_requests WHERE schoolId = :schoolId ORDER BY requestedAt DESC")
    suspend fun getAllSnapshot(schoolId: Long): List<RepairRequestEntity>
}
