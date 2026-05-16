package com.nammashalli.inventory.utils

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtpManager @Inject constructor() {
    private val otpStore = mutableMapOf<String, OtpEntry>()

    companion object {
        private const val OTP_EXPIRY_MS = 5 * 60 * 1000L
        private const val MAX_ATTEMPTS = 5
        private const val MAX_RESENDS = 3
    }

    fun generateOtp(phone: String): String {
        val otp = (100000..999999).random().toString()
        otpStore[phone] = OtpEntry(
            otp = otp,
            expiresAt = System.currentTimeMillis() + OTP_EXPIRY_MS,
            attempts = 0,
            resendCount = (otpStore[phone]?.resendCount ?: 0) + 1
        )
        return otp
    }

    fun verifyOtp(phone: String, enteredOtp: String): OtpResult {
        val entry = otpStore[phone]
            ?: return OtpResult.NotFound
        if (System.currentTimeMillis() > entry.expiresAt)
            return OtpResult.Expired
        if (entry.attempts >= MAX_ATTEMPTS)
            return OtpResult.TooManyAttempts
        if (entry.otp != enteredOtp) {
            otpStore[phone] = entry.copy(attempts = entry.attempts + 1)
            return OtpResult.Invalid(MAX_ATTEMPTS - entry.attempts - 1)
        }
        otpStore.remove(phone)
        return OtpResult.Success
    }

    fun canResend(phone: String): Boolean {
        return (otpStore[phone]?.resendCount ?: 0) < MAX_RESENDS
    }

    fun getRemainingSeconds(phone: String): Long {
        val entry = otpStore[phone] ?: return 0
        return maxOf(0, (entry.expiresAt - System.currentTimeMillis()) / 1000)
    }

    fun clearOtp(phone: String) {
        otpStore.remove(phone)
    }
}

data class OtpEntry(
    val otp: String,
    val expiresAt: Long,
    val attempts: Int,
    val resendCount: Int
)

sealed class OtpResult {
    data object Success : OtpResult()
    data object Expired : OtpResult()
    data object NotFound : OtpResult()
    data object TooManyAttempts : OtpResult()
    data class Invalid(val attemptsLeft: Int) : OtpResult()
}
