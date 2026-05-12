package com.example.nammashalli.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_USER_ID = longPreferencesKey("user_id")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_SCHOOL_NAME = stringPreferencesKey("school_name")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_PHONE = stringPreferencesKey("phone")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_GROQ_API_KEY = stringPreferencesKey("groq_api_key")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_THEME = stringPreferencesKey("theme_preference")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val KEY_API_KEY_VALID = booleanPreferencesKey("api_key_valid")
        val KEY_PROFILE_PHOTO = stringPreferencesKey("profile_photo_path")
    }

    val userId: Flow<Long> = dataStore.data.map { it[KEY_USER_ID] ?: -1L }
    val userName: Flow<String> = dataStore.data.map { it[KEY_USER_NAME] ?: "" }
    val schoolName: Flow<String> = dataStore.data.map { it[KEY_SCHOOL_NAME] ?: "" }
    val userRole: Flow<String> = dataStore.data.map { it[KEY_USER_ROLE] ?: "" }
    val email: Flow<String> = dataStore.data.map { it[KEY_EMAIL] ?: "" }
    val phone: Flow<String> = dataStore.data.map { it[KEY_PHONE] ?: "" }
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[KEY_IS_LOGGED_IN] ?: false }
    val groqApiKey: Flow<String?> = dataStore.data.map { it[KEY_GROQ_API_KEY] }
    val theme: Flow<String> = dataStore.data.map { it[KEY_THEME] ?: "System" }
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_NOTIFICATIONS] ?: true }
    val apiKeyValid: Flow<Boolean?> = dataStore.data.map { it[KEY_API_KEY_VALID] }
    val profilePhotoPath: Flow<String?> = dataStore.data.map { it[KEY_PROFILE_PHOTO] }

    suspend fun saveSession(
        userId: Long,
        userName: String,
        schoolName: String,
        role: String,
        phone: String,
        email: String = "",
        groqApiKey: String? = null
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_NAME] = userName
            prefs[KEY_SCHOOL_NAME] = schoolName
            prefs[KEY_USER_ROLE] = role
            prefs[KEY_PHONE] = phone
            prefs[KEY_EMAIL] = email
            prefs[KEY_IS_LOGGED_IN] = true
            groqApiKey?.let { prefs[KEY_GROQ_API_KEY] = it }
        }
    }

    suspend fun updateField(block: MutablePreferences.() -> Unit) {
        dataStore.edit { it.block() }
    }

    suspend fun saveApiKey(encryptedKey: String, isValid: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_GROQ_API_KEY] = encryptedKey
            prefs[KEY_API_KEY_VALID] = isValid
        }
    }

    suspend fun setApiKeyValid(valid: Boolean) {
        dataStore.edit { it[KEY_API_KEY_VALID] = valid }
    }

    suspend fun setTheme(theme: String) {
        dataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun setNotifications(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    }

    suspend fun setProfilePhoto(path: String) {
        dataStore.edit { it[KEY_PROFILE_PHOTO] = path }
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }

    suspend fun getCurrentUserId(): Long {
        return dataStore.data.map { it[KEY_USER_ID] ?: -1L }.first()
    }
}
