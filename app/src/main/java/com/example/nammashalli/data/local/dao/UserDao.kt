package com.example.nammashalli.data.local.dao

import androidx.room.*
import com.example.nammashalli.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone AND passwordHash = :hash LIMIT 1")
    suspend fun authenticate(phone: String, hash: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun emailExists(email: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE phoneNumber = :phone")
    suspend fun phoneExists(phone: String): Int

    @Query("UPDATE users SET fullName = :name, updatedAt = :ts WHERE id = :id")
    suspend fun updateFullName(id: Long, name: String, ts: Long)

    @Query("UPDATE users SET email = :email, updatedAt = :ts WHERE id = :id")
    suspend fun updateEmail(id: Long, email: String, ts: Long)

    @Query("UPDATE users SET phoneNumber = :phone, updatedAt = :ts WHERE id = :id")
    suspend fun updatePhone(id: Long, phone: String, ts: Long)

    @Query("UPDATE users SET role = :role, updatedAt = :ts WHERE id = :id")
    suspend fun updateRole(id: Long, role: String, ts: Long)

    @Query("UPDATE users SET schoolName = :school, updatedAt = :ts WHERE id = :id")
    suspend fun updateSchool(id: Long, school: String, ts: Long)

    @Query("UPDATE users SET profilePhotoPath = :path, updatedAt = :ts WHERE id = :id")
    suspend fun updateProfilePhoto(id: Long, path: String, ts: Long)
}
