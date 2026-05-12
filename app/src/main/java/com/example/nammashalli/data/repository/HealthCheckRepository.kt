package com.example.nammashalli.data.repository

import com.example.nammashalli.data.local.dao.HealthCheckDao
import com.example.nammashalli.data.local.entities.HealthCheckEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthCheckRepository @Inject constructor(private val dao: HealthCheckDao) {

    suspend fun insert(check: HealthCheckEntity): Long = dao.insert(check)

    suspend fun insertAll(checks: List<HealthCheckEntity>) = dao.insertAll(checks)

    fun getForAsset(assetId: Long): Flow<List<HealthCheckEntity>> = dao.getForAsset(assetId)

    suspend fun getRecentForAsset(assetId: Long): List<HealthCheckEntity> =
        dao.getRecentForAsset(assetId)

    fun getBySchool(schoolId: Long): Flow<List<HealthCheckEntity>> = dao.getBySchool(schoolId)

    suspend fun countBySchool(schoolId: Long): Int = dao.countBySchool(schoolId)

    suspend fun getRecentBySchool(schoolId: Long): List<HealthCheckEntity> =
        dao.getRecentBySchool(schoolId)
}
