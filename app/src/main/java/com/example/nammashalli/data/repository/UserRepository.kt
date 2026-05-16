package com.nammashalli.inventory.data.repository

import com.nammashalli.inventory.data.local.dao.UserDao
import com.nammashalli.inventory.data.local.entities.UserEntity
import com.nammashalli.inventory.utils.EncryptionUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val userDao: UserDao) {

    suspend fun register(user: UserEntity): Result<Long> {
        return try {
            if (userDao.emailExists(user.email) > 0)
                return Result.failure(Exception("Email already registered"))
            if (userDao.phoneExists(user.phoneNumber) > 0)
                return Result.failure(Exception("Phone already registered"))
            val id = userDao.insert(user)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findByPhone(phone: String): UserEntity? = userDao.findByPhone(phone)

    suspend fun authenticate(phone: String, password: String): UserEntity? {
        val user = userDao.findByPhone(phone) ?: return null
        return if (EncryptionUtil.verifyPassword(password, user.passwordHash)) user else null
    }

    suspend fun getById(id: Long): UserEntity? = userDao.getById(id)

    suspend fun update(user: UserEntity) = userDao.update(user)

    suspend fun phoneExists(phone: String): Boolean = userDao.phoneExists(phone) > 0

    suspend fun updateFullName(id: Long, name: String) =
        userDao.updateFullName(id, name, System.currentTimeMillis())

    suspend fun updateEmail(id: Long, email: String) =
        userDao.updateEmail(id, email, System.currentTimeMillis())

    suspend fun updatePhone(id: Long, phone: String) =
        userDao.updatePhone(id, phone, System.currentTimeMillis())

    suspend fun updateRole(id: Long, role: String) =
        userDao.updateRole(id, role, System.currentTimeMillis())

    suspend fun updateSchool(id: Long, school: String) =
        userDao.updateSchool(id, school, System.currentTimeMillis())

    suspend fun updateProfilePhoto(id: Long, path: String) =
        userDao.updateProfilePhoto(id, path, System.currentTimeMillis())
}
