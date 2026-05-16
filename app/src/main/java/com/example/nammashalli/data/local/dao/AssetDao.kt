package com.nammashalli.inventory.data.local.dao

import androidx.room.*
import com.nammashalli.inventory.data.local.entities.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: AssetEntity): Long

    @Update
    suspend fun update(asset: AssetEntity)

    @Delete
    suspend fun delete(asset: AssetEntity)

    @Query("SELECT * FROM assets WHERE schoolId = :schoolId ORDER BY createdAt DESC")
    fun getAllBySchool(schoolId: Long): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AssetEntity?

    @Query("SELECT * FROM assets WHERE schoolId = :schoolId AND currentStatus = :status")
    fun getByStatus(schoolId: Long, status: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE schoolId = :schoolId AND category = :category")
    fun getByCategory(schoolId: Long, category: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE schoolId = :schoolId AND (LOWER(assetName) LIKE '%' || LOWER(:query) || '%' OR LOWER(assetId) LIKE '%' || LOWER(:query) || '%')")
    fun search(schoolId: Long, query: String): Flow<List<AssetEntity>>

    @Query("SELECT COUNT(*) FROM assets WHERE schoolId = :schoolId")
    suspend fun countAll(schoolId: Long): Int

    @Query("SELECT COUNT(*) FROM assets WHERE schoolId = :schoolId AND currentStatus = 'Good'")
    suspend fun countGood(schoolId: Long): Int

    @Query("SELECT COUNT(*) FROM assets WHERE schoolId = :schoolId AND currentStatus = 'Fair'")
    suspend fun countFair(schoolId: Long): Int

    @Query("SELECT COUNT(*) FROM assets WHERE schoolId = :schoolId AND currentStatus = 'NeedsRepair'")
    suspend fun countNeedsRepair(schoolId: Long): Int

    @Query("SELECT COUNT(*) FROM assets WHERE schoolId = :schoolId AND currentStatus = 'Lost'")
    suspend fun countLost(schoolId: Long): Int

    @Query("SELECT MAX(CAST(SUBSTR(assetId, 7) AS INTEGER)) FROM assets WHERE schoolId = :schoolId")
    suspend fun getMaxAssetNumber(schoolId: Long): Int?

    @Query("UPDATE assets SET currentStatus = :status, updatedAt = :now WHERE id = :assetId")
    suspend fun updateStatus(assetId: Long, status: String, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM assets WHERE schoolId = :schoolId ORDER BY createdAt DESC")
    suspend fun getAllBySchoolSnapshot(schoolId: Long): List<AssetEntity>
}
