package com.nammashalli.inventory.data.local.dao

import androidx.room.*
import com.nammashalli.inventory.data.local.entities.HealthCheckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthCheckDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(check: HealthCheckEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(checks: List<HealthCheckEntity>)

    @Query("SELECT * FROM health_checks WHERE assetId = :assetId ORDER BY checkedAt DESC")
    fun getForAsset(assetId: Long): Flow<List<HealthCheckEntity>>

    @Query("SELECT * FROM health_checks WHERE assetId = :assetId ORDER BY checkedAt DESC LIMIT :limit")
    suspend fun getRecentForAsset(assetId: Long, limit: Int = 5): List<HealthCheckEntity>

    @Query("SELECT * FROM health_checks WHERE schoolId = :schoolId ORDER BY checkedAt DESC")
    fun getBySchool(schoolId: Long): Flow<List<HealthCheckEntity>>

    @Query("SELECT COUNT(*) FROM health_checks WHERE schoolId = :schoolId")
    suspend fun countBySchool(schoolId: Long): Int

    @Query("SELECT * FROM health_checks WHERE schoolId = :schoolId ORDER BY checkedAt DESC LIMIT 20")
    suspend fun getRecentBySchool(schoolId: Long): List<HealthCheckEntity>
}
