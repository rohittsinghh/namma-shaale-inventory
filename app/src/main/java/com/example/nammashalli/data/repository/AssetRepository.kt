package com.example.nammashalli.data.repository

import com.example.nammashalli.data.local.dao.AssetDao
import com.example.nammashalli.data.local.entities.AssetEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetRepository @Inject constructor(private val assetDao: AssetDao) {

    fun getAllAssets(schoolId: Long): Flow<List<AssetEntity>> = assetDao.getAllBySchool(schoolId)

    fun searchAssets(schoolId: Long, query: String): Flow<List<AssetEntity>> =
        assetDao.search(schoolId, query)

    fun getAssetsByStatus(schoolId: Long, status: String): Flow<List<AssetEntity>> =
        assetDao.getByStatus(schoolId, status)

    suspend fun getById(id: Long): AssetEntity? = assetDao.getById(id)

    suspend fun insertAsset(asset: AssetEntity): Long = assetDao.insert(asset)

    suspend fun updateAsset(asset: AssetEntity) = assetDao.update(asset)

    suspend fun deleteAsset(asset: AssetEntity) = assetDao.delete(asset)

    suspend fun updateStatus(assetId: Long, status: String) = assetDao.updateStatus(assetId, status)

    suspend fun generateNextAssetId(schoolId: Long): String {
        val maxNum = assetDao.getMaxAssetNumber(schoolId) ?: 0
        return "ASSET-${String.format("%03d", maxNum + 1)}"
    }

    suspend fun getStatusCounts(schoolId: Long): AssetStatusCounts {
        return AssetStatusCounts(
            total = assetDao.countAll(schoolId),
            good = assetDao.countGood(schoolId),
            fair = assetDao.countFair(schoolId),
            needsRepair = assetDao.countNeedsRepair(schoolId),
            lost = assetDao.countLost(schoolId)
        )
    }

    suspend fun getAllSnapshot(schoolId: Long): List<AssetEntity> = assetDao.getAllBySchoolSnapshot(schoolId)
}

data class AssetStatusCounts(
    val total: Int,
    val good: Int,
    val fair: Int,
    val needsRepair: Int,
    val lost: Int
)
