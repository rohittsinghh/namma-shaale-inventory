package com.example.nammashalli.di

import android.content.Context
import androidx.room.Room
import com.example.nammashalli.data.local.AppDatabase
import com.example.nammashalli.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "namma_shaale_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideAssetDao(db: AppDatabase): AssetDao = db.assetDao()
    @Provides fun provideHealthCheckDao(db: AppDatabase): HealthCheckDao = db.healthCheckDao()
    @Provides fun provideIssueLogDao(db: AppDatabase): IssueLogDao = db.issueLogDao()
    @Provides fun provideRepairRequestDao(db: AppDatabase): RepairRequestDao = db.repairRequestDao()
}
