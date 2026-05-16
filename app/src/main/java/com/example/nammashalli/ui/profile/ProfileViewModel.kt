package com.nammashalli.inventory.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammashalli.inventory.data.repository.UserRepository
import com.nammashalli.inventory.utils.EncryptionUtil
import com.nammashalli.inventory.utils.ImageUtil
import com.nammashalli.inventory.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

data class ProfileState(
    val userId: Long = -1L,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val schoolName: String = "",
    val profilePhotoPath: String? = null,
    val rawApiKey: String? = null,
    val apiKeyVisible: Boolean = false,
    val apiKeyValid: Boolean? = null,
    val isTestingApiKey: Boolean = false,
    val theme: String = "System",
    val notificationsEnabled: Boolean = true,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

data class ProfileMessage(val text: String, val isError: Boolean)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private var autoHideJob: Job? = null

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _messages = Channel<ProfileMessage>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    init {
        viewModelScope.launch {
            sessionManager.userId.collect { uid ->
                if (uid > 0L) {
                    val user = withContext(Dispatchers.IO) { userRepository.getById(uid) }
                    _state.value = _state.value.copy(
                        userId = uid,
                        fullName = user?.fullName ?: "",
                        email = user?.email ?: "",
                        phone = user?.phoneNumber ?: "",
                        role = user?.role ?: "",
                        schoolName = user?.schoolName ?: "",
                        profilePhotoPath = user?.profilePhotoPath,
                        isLoading = false
                    )
                }
            }
        }
        viewModelScope.launch {
            sessionManager.theme.collect { _state.value = _state.value.copy(theme = it) }
        }
        viewModelScope.launch {
            sessionManager.notificationsEnabled.collect { _state.value = _state.value.copy(notificationsEnabled = it) }
        }
        viewModelScope.launch {
            sessionManager.apiKeyValid.collect { _state.value = _state.value.copy(apiKeyValid = it) }
        }
        viewModelScope.launch {
            sessionManager.groqApiKey.collect { encryptedKey ->
                val raw = encryptedKey?.let {
                    try { EncryptionUtil.decryptApiKey(it) } catch (e: Exception) { null }
                }
                _state.value = _state.value.copy(rawApiKey = raw)
            }
        }
    }

    fun updateFullName(name: String) = viewModelScope.launch {
        val trimmed = name.trim()
        if (trimmed.length < 3) { showError("Name must be at least 3 characters"); return@launch }
        save {
            userRepository.updateFullName(_state.value.userId, trimmed)
            sessionManager.updateField { this[SessionManager.KEY_USER_NAME] = trimmed }
            _state.value = _state.value.copy(fullName = trimmed)
            showSuccess("Name updated")
        }
    }

    fun updateEmail(email: String) = viewModelScope.launch {
        val trimmed = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            showError("Invalid email address"); return@launch
        }
        save {
            userRepository.updateEmail(_state.value.userId, trimmed)
            sessionManager.updateField { this[SessionManager.KEY_EMAIL] = trimmed }
            _state.value = _state.value.copy(email = trimmed)
            showSuccess("Email updated")
        }
    }

    fun updatePhone(phone: String) = viewModelScope.launch {
        val digits = phone.filter { it.isDigit() }
        if (digits.length != 10) { showError("Enter a valid 10-digit mobile number"); return@launch }
        val formatted = "+91 ${digits.take(5)} ${digits.takeLast(5)}"
        save {
            userRepository.updatePhone(_state.value.userId, formatted)
            sessionManager.updateField { this[SessionManager.KEY_PHONE] = formatted }
            _state.value = _state.value.copy(phone = formatted)
            showSuccess("Phone updated")
        }
    }

    fun updateRole(role: String) = viewModelScope.launch {
        save {
            userRepository.updateRole(_state.value.userId, role)
            sessionManager.updateField { this[SessionManager.KEY_USER_ROLE] = role }
            _state.value = _state.value.copy(role = role)
            showSuccess("Role updated")
        }
    }

    fun updateSchool(school: String) = viewModelScope.launch {
        val trimmed = school.trim()
        if (trimmed.isBlank()) { showError("School name cannot be empty"); return@launch }
        save {
            userRepository.updateSchool(_state.value.userId, trimmed)
            sessionManager.updateField { this[SessionManager.KEY_SCHOOL_NAME] = trimmed }
            _state.value = _state.value.copy(schoolName = trimmed)
            showSuccess("School name updated")
        }
    }

    fun updateProfilePhoto(context: Context, uri: Uri) = viewModelScope.launch {
        _state.value = _state.value.copy(isSaving = true)
        val path = withContext(Dispatchers.IO) { ImageUtil.compressAndSave(context, uri) }
        if (path == null) { showError("Could not process photo"); return@launch }
        try {
            userRepository.updateProfilePhoto(_state.value.userId, path)
            sessionManager.setProfilePhoto(path)
            _state.value = _state.value.copy(profilePhotoPath = path, isSaving = false)
            showSuccess("Photo updated")
        } catch (e: Exception) {
            _state.value = _state.value.copy(isSaving = false)
            showError("Photo update failed")
        }
    }

    fun setApiKeyVisible(visible: Boolean) {
        autoHideJob?.cancel()
        _state.value = _state.value.copy(apiKeyVisible = visible)
        if (visible) {
            autoHideJob = viewModelScope.launch {
                delay(10_000)
                _state.value = _state.value.copy(apiKeyVisible = false)
            }
        }
    }

    fun testApiKey(key: String? = null) = viewModelScope.launch {
        val keyToTest = (key?.trim() ?: _state.value.rawApiKey?.trim())
        if (keyToTest.isNullOrBlank()) { showError("No API key to test"); return@launch }
        _state.value = _state.value.copy(isTestingApiKey = true)
        val isValid = withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://api.groq.com/openai/v1/models")
                    .addHeader("Authorization", "Bearer $keyToTest")
                    .get()
                    .build()
                okHttpClient.newCall(request).execute().use { it.isSuccessful }
            } catch (e: Exception) {
                false
            }
        }
        sessionManager.setApiKeyValid(isValid)
        _state.value = _state.value.copy(isTestingApiKey = false, apiKeyValid = isValid)
        showSuccess(if (isValid) "API key is valid" else "API key is invalid")
    }

    fun saveApiKey(newKey: String) = viewModelScope.launch {
        if (newKey.isBlank()) { showError("API key cannot be empty"); return@launch }
        _state.value = _state.value.copy(isSaving = true)
        val encrypted = withContext(Dispatchers.IO) { EncryptionUtil.encryptApiKey(newKey.trim()) }
        sessionManager.saveApiKey(encrypted, false)
        _state.value = _state.value.copy(rawApiKey = newKey.trim(), apiKeyValid = null, isSaving = false)
        showSuccess("API key saved — tap Test to validate")
    }

    fun setTheme(theme: String) = viewModelScope.launch { sessionManager.setTheme(theme) }

    fun setNotifications(enabled: Boolean) = viewModelScope.launch { sessionManager.setNotifications(enabled) }

    fun clearCache(context: Context) = viewModelScope.launch {
        withContext(Dispatchers.IO) { context.cacheDir.deleteRecursively() }
        showSuccess("Cache cleared")
    }

    fun logout(onComplete: () -> Unit) = viewModelScope.launch {
        sessionManager.clearSession()
        onComplete()
    }

    private suspend fun save(block: suspend () -> Unit) {
        _state.value = _state.value.copy(isSaving = true)
        try {
            withContext(Dispatchers.IO) { block() }
        } catch (e: Exception) {
            showError(e.message ?: "Update failed")
        } finally {
            _state.value = _state.value.copy(isSaving = false)
        }
    }

    private fun showSuccess(msg: String) {
        _state.value = _state.value.copy(isSaving = false)
        viewModelScope.launch { _messages.send(ProfileMessage(msg, isError = false)) }
    }

    private fun showError(msg: String) {
        _state.value = _state.value.copy(isSaving = false)
        viewModelScope.launch { _messages.send(ProfileMessage(msg, isError = true)) }
    }
}
