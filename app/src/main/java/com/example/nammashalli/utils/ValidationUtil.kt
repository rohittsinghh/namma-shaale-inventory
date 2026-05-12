package com.example.nammashalli.utils

object ValidationUtil {

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPhone(phone: String): Boolean {
        val cleaned = phone.replace("+91", "").replace(" ", "").replace("-", "")
        return cleaned.length == 10 && cleaned.all { it.isDigit() }
    }

    fun cleanPhone(phone: String): String {
        return phone.replace("+91", "").replace(" ", "").replace("-", "")
    }

    fun formatPhone(phone: String): String {
        val cleaned = cleanPhone(phone)
        return if (cleaned.startsWith("+91")) cleaned else "+91$cleaned"
    }

    fun validatePassword(password: String): PasswordValidation {
        val errors = mutableListOf<String>()
        if (password.length < 8) errors.add("At least 8 characters")
        if (!password.any { it.isUpperCase() }) errors.add("One uppercase letter")
        if (!password.any { it.isLowerCase() }) errors.add("One lowercase letter")
        if (!password.any { it.isDigit() }) errors.add("One number")
        if (!password.any { "!@#\$%^&*()_+-=[]{}|;':\",./<>?".contains(it) }) errors.add("One special character")
        return PasswordValidation(errors.isEmpty(), errors)
    }

    fun getPasswordStrength(password: String): PasswordStrength {
        val validation = validatePassword(password)
        return when {
            password.length < 4 -> PasswordStrength.WEAK
            !validation.isValid && validation.errors.size > 2 -> PasswordStrength.WEAK
            !validation.isValid -> PasswordStrength.MODERATE
            password.length >= 12 -> PasswordStrength.STRONG
            else -> PasswordStrength.GOOD
        }
    }

    fun isValidName(name: String): Boolean = name.trim().length in 2..50
    fun isValidSchoolName(name: String): Boolean = name.trim().length in 3..50
    fun isValidCost(cost: String): Boolean = cost.toDoubleOrNull()?.let { it > 0 } ?: false
}

data class PasswordValidation(val isValid: Boolean, val errors: List<String>)

enum class PasswordStrength { WEAK, MODERATE, GOOD, STRONG }
