package com.nammashalli.inventory.data.local.dao

import androidx.room.*
import com.nammashalli.inventory.data.local.entities.IssueLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IssueLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(issue: IssueLogEntity): Long

    @Update
    suspend fun update(issue: IssueLogEntity)

    @Query("SELECT * FROM issue_logs WHERE schoolId = :schoolId ORDER BY reportedAt DESC")
    fun getAllBySchool(schoolId: Long): Flow<List<IssueLogEntity>>

    @Query("SELECT * FROM issue_logs WHERE assetId = :assetId ORDER BY reportedAt DESC")
    fun getForAsset(assetId: Long): Flow<List<IssueLogEntity>>

    @Query("SELECT * FROM issue_logs WHERE schoolId = :schoolId AND status = :status ORDER BY reportedAt DESC")
    fun getByStatus(schoolId: Long, status: String): Flow<List<IssueLogEntity>>

    @Query("SELECT COUNT(*) FROM issue_logs WHERE schoolId = :schoolId AND status = 'Open'")
    suspend fun countOpen(schoolId: Long): Int

    @Query("SELECT * FROM issue_logs WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): IssueLogEntity?

    @Query("UPDATE issue_logs SET status = 'Resolved' WHERE id = :id")
    suspend fun resolve(id: Long)

    @Query("SELECT * FROM issue_logs WHERE schoolId = :schoolId ORDER BY reportedAt DESC")
    suspend fun getAllSnapshot(schoolId: Long): List<IssueLogEntity>
}
