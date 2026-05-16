package com.nammashalli.inventory.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammashalli.inventory.data.local.entities.UserEntity
import com.nammashalli.inventory.data.repository.UserRepository
import com.nammashalli.inventory.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val schoolName: String = "",
    val role: String = "Teacher",
    val groqApiKey: String = "",
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val passwordStrength: PasswordStrength = PasswordStrength.WEAK
)

data class SignInState(
    val phone: String = "",
    val cleanPhone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val otpSent: Boolean = false,
    val generatedOtp: String = ""
)

data class OtpState(
    val otp: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val remainingSeconds: Long = 300L,
    val canResend: Boolean = true,
    val generatedOtp: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val otpManager: OtpManager
) : ViewModel() {

    private val _signUpState = MutableStateFlow(SignUpState())
    val signUpState: StateFlow<SignUpState> = _signUpState.asStateFlow()

    private val _signInState = MutableStateFlow(SignInState())
    val signInState: StateFlow<SignInState> = _signInState.asStateFlow()

    private val _otpState = MutableStateFlow(OtpState())
    val otpState: StateFlow<OtpState> = _otpState.asStateFlow()

    // SignUp field updates
    fun updateSignUpField(field: SignUpField, value: String) {
        _signUpState.value = when (field) {
            SignUpField.FULL_NAME -> _signUpState.value.copy(fullName = value)
            SignUpField.EMAIL -> _signUpState.value.copy(email = value)
            SignUpField.PHONE -> _signUpState.value.copy(phone = value)
            SignUpField.PASSWORD -> _signUpState.value.copy(
                password = value,
                passwordStrength = ValidationUtil.getPasswordStrength(value)
            )
            SignUpField.CONFIRM_PASSWORD -> _signUpState.value.copy(confirmPassword = value)
            SignUpField.SCHOOL_NAME -> _signUpState.value.copy(schoolName = value)
            SignUpField.GROQ_API_KEY -> _signUpState.value.copy(groqApiKey = value)
        }
    }

    fun updateRole(role: String) {
        _signUpState.value = _signUpState.value.copy(role = role)
    }

    fun updateTerms(accepted: Boolean) {
        _signUpState.value = _signUpState.value.copy(termsAccepted = accepted)
    }

    fun signUp() = viewModelScope.launch {
        val s = _signUpState.value
        val error = validateSignUp(s)
        if (error != null) {
            _signUpState.value = s.copy(error = error)
            return@launch
        }
        _signUpState.value = s.copy(isLoading = true, error = null)
        val cleanPhone = ValidationUtil.cleanPhone(s.phone)
        val user = UserEntity(
            fullName = s.fullName.trim(),
            email = s.email.trim().lowercase(),
            phoneNumber = cleanPhone,
            passwordHash = EncryptionUtil.hashPassword(s.password),
            schoolName = s.schoolName.trim(),
            role = s.role,
            groqApiKey = s.groqApiKey.takeIf { it.isNotBlank() }?.let { EncryptionUtil.simpleEncrypt(it) }
        )
        val result = userRepository.register(user)
        if (result.isSuccess) {
            _signUpState.value = s.copy(isLoading = false, success = true)
        } else {
            _signUpState.value = s.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Registration failed")
        }
    }

    private fun validateSignUp(s: SignUpState): String? {
        if (!ValidationUtil.isValidName(s.fullName)) return "Enter a valid full name (2-50 chars)"
        if (!ValidationUtil.isValidEmail(s.email)) return "Enter a valid email address"
        if (!ValidationUtil.isValidPhone(s.phone)) return "Enter a valid 10-digit phone number"
        val pwv = ValidationUtil.validatePassword(s.password)
        if (!pwv.isValid) return "Password needs: ${pwv.errors.joinToString(", ")}"
        if (s.password != s.confirmPassword) return "Passwords do not match"
        if (!ValidationUtil.isValidSchoolName(s.schoolName)) return "Enter a valid school name"
        if (!s.termsAccepted) return "Please accept the terms and conditions"
        return null
    }

    // SignIn
    fun updatePhone(phone: String) {
        _signInState.value = _signInState.value.copy(phone = phone, error = null)
    }

    fun sendOtp() = viewModelScope.launch {
        val phone = ValidationUtil.cleanPhone(_signInState.value.phone)
        if (!ValidationUtil.isValidPhone(phone)) {
            _signInState.value = _signInState.value.copy(error = "Enter a valid 10-digit phone number")
            return@launch
        }
        _signInState.value = _signInState.value.copy(isLoading = true, error = null)
        val exists = userRepository.phoneExists(phone)
        if (!exists) {
            _signInState.value = _signInState.value.copy(isLoading = false, error = "Phone number not registered. Please sign up first.")
            return@launch
        }
        val otp = otpManager.generateOtp(phone)
        _signInState.value = _signInState.value.copy(isLoading = false, otpSent = true, generatedOtp = otp, cleanPhone = phone)
        _otpState.value = OtpState(phone = phone, remainingSeconds = 300L, generatedOtp = otp)
    }

    fun initOtpState(phone: String, generatedOtp: String) {
        if (_otpState.value.phone.isEmpty()) {
            _otpState.value = OtpState(phone = phone, remainingSeconds = 300L, generatedOtp = generatedOtp)
        }
    }

    // OTP
    fun updateOtp(otp: String) {
        _otpState.value = _otpState.value.copy(otp = otp, error = null)
    }

    fun verifyOtp() = viewModelScope.launch {
        val state = _otpState.value
        _otpState.value = state.copy(isLoading = true, error = null)
        val result = otpManager.verifyOtp(state.phone, state.otp)
        when (result) {
            is OtpResult.Success -> {
                val user = userRepository.findByPhone(state.phone)
                if (user != null) {
                    // Decrypt DB key (simpleEncrypt/Base64) then re-encrypt with Keystore for DataStore
                    val sessionKey = user.groqApiKey?.let { dbKey ->
                        val plain = EncryptionUtil.simpleDecrypt(dbKey)
                        if (plain.isNotBlank()) EncryptionUtil.encryptApiKey(plain) else null
                    }
                    sessionManager.saveSession(
                        userId = user.id,
                        userName = user.fullName,
                        schoolName = user.schoolName,
                        role = user.role,
                        phone = user.phoneNumber,
                        email = user.email,
                        groqApiKey = sessionKey
                    )
                    _otpState.value = state.copy(isLoading = false, success = true)
                } else {
                    _otpState.value = state.copy(isLoading = false, error = "User not found")
                }
            }
            is OtpResult.Expired -> _otpState.value = state.copy(isLoading = false, error = "OTP expired. Please resend.")
            is OtpResult.Invalid -> _otpState.value = state.copy(isLoading = false, error = "Wrong OTP. ${result.attemptsLeft} attempts left.")
            is OtpResult.TooManyAttempts -> _otpState.value = state.copy(isLoading = false, error = "Too many attempts. Please resend OTP.")
            is OtpResult.NotFound -> _otpState.value = state.copy(isLoading = false, error = "OTP not found. Please resend.")
        }
    }

    fun resendOtp() = viewModelScope.launch {
        val phone = _otpState.value.phone
        if (!otpManager.canResend(phone)) {
            _otpState.value = _otpState.value.copy(error = "Max resend attempts reached. Please try again later.")
            return@launch
        }
        val otp = otpManager.generateOtp(phone)
        _otpState.value = _otpState.value.copy(remainingSeconds = 300L, error = null, generatedOtp = otp, otp = "")
    }

    fun tickOtp() {
        val current = _otpState.value.remainingSeconds
        if (current > 0) _otpState.value = _otpState.value.copy(remainingSeconds = current - 1)
    }

    fun clearSignUpError() { _signUpState.value = _signUpState.value.copy(error = null) }
    fun clearSignInError() { _signInState.value = _signInState.value.copy(error = null) }
}

enum class SignUpField {
    FULL_NAME, EMAIL, PHONE, PASSWORD, CONFIRM_PASSWORD, SCHOOL_NAME, GROQ_API_KEY
}
