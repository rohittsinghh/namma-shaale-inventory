package com.example.nammashalli.data.repository

import com.example.nammashalli.data.local.dao.IssueLogDao
import com.example.nammashalli.data.local.entities.IssueLogEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IssueRepository @Inject constructor(private val dao: IssueLogDao) {

    suspend fun insert(issue: IssueLogEntity): Long = dao.insert(issue)

    fun getAllBySchool(schoolId: Long): Flow<List<IssueLogEntity>> = dao.getAllBySchool(schoolId)

    fun getForAsset(assetId: Long): Flow<List<IssueLogEntity>> = dao.getForAsset(assetId)

    fun getByStatus(schoolId: Long, status: String): Flow<List<IssueLogEntity>> =
        dao.getByStatus(schoolId, status)

    suspend fun countOpen(schoolId: Long): Int = dao.countOpen(schoolId)

    suspend fun resolve(id: Long) = dao.resolve(id)

    suspend fun getById(id: Long): IssueLogEntity? = dao.getById(id)

    suspend fun getAllSnapshot(schoolId: Long): List<IssueLogEntity> = dao.getAllSnapshot(schoolId)
}
