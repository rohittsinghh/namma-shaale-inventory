package com.example.nammashalli.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nammashalli.data.local.dao.*
import com.example.nammashalli.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        AssetEntity::class,
        HealthCheckEntity::class,
        IssueLogEntity::class,
        RepairRequestEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun assetDao(): AssetDao
    abstract fun healthCheckDao(): HealthCheckDao
    abstract fun issueLogDao(): IssueLogDao
    abstract fun repairRequestDao(): RepairRequestDao
}
