package com.nammashalli.inventory.data.repository

import com.nammashalli.inventory.data.local.dao.RepairRequestDao
import com.nammashalli.inventory.data.local.entities.RepairRequestEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepairRepository @Inject constructor(private val dao: RepairRequestDao) {

    suspend fun insert(request: RepairRequestEntity): Long = dao.insert(request)

    suspend fun insertAll(requests: List<RepairRequestEntity>) = dao.insertAll(requests)

    fun getAllBySchool(schoolId: Long): Flow<List<RepairRequestEntity>> = dao.getAllBySchool(schoolId)

    fun getPending(schoolId: Long): Flow<List<RepairRequestEntity>> = dao.getPending(schoolId)

    fun getCompleted(schoolId: Long): Flow<List<RepairRequestEntity>> = dao.getCompleted(schoolId)

    fun getForAsset(assetId: Long): Flow<List<RepairRequestEntity>> = dao.getForAsset(assetId)

    suspend fun countPending(schoolId: Long): Int = dao.countPending(schoolId)

    suspend fun markCompleted(id: Long) = dao.markCompleted(id)

    suspend fun update(request: RepairRequestEntity) = dao.update(request)

    suspend fun getAllSnapshot(schoolId: Long): List<RepairRequestEntity> = dao.getAllSnapshot(schoolId)
}
