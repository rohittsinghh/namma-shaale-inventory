package com.nammashalli.inventory

import com.nammashalli.inventory.utils.PasswordStrength
import com.nammashalli.inventory.utils.ValidationUtil
import org.junit.Assert.*
import org.junit.Test

class ValidationUtilTest {

    // --- Phone number validation ---

    @Test
    fun `phone number with 10 digits is valid`() {
        assertTrue(ValidationUtil.isValidPhone("9876543210"))
    }

    @Test
    fun `phone number with country code is valid`() {
        assertTrue(ValidationUtil.isValidPhone("+919876543210"))
    }

    @Test
    fun `phone number with 9 digits is invalid`() {
        assertFalse(ValidationUtil.isValidPhone("987654321"))
    }

    @Test
    fun `phone number with letters is invalid`() {
        assertFalse(ValidationUtil.isValidPhone("98765432ab"))
    }

    @Test
    fun `blank phone number is invalid`() {
        assertFalse(ValidationUtil.isValidPhone(""))
    }

    // --- Password validation ---

    @Test
    fun `strong password passes all validations`() {
        val result = ValidationUtil.validatePassword("Secure@123")
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `password shorter than 8 characters fails validation`() {
        val result = ValidationUtil.validatePassword("Ab1!")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("8") })
    }

    @Test
    fun `password without uppercase fails validation`() {
        val result = ValidationUtil.validatePassword("secure@123")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("uppercase", ignoreCase = true) })
    }

    @Test
    fun `password without digit fails validation`() {
        val result = ValidationUtil.validatePassword("SecurePass!")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("number", ignoreCase = true) })
    }

    // --- Password strength ---

    @Test
    fun `very short password is WEAK`() {
        assertEquals(PasswordStrength.WEAK, ValidationUtil.getPasswordStrength("ab"))
    }

    @Test
    fun `strong 12-char password with all criteria is STRONG`() {
        assertEquals(PasswordStrength.STRONG, ValidationUtil.getPasswordStrength("Secure@123456"))
    }

    // --- Name validation ---

    @Test
    fun `name with 2 or more characters is valid`() {
        assertTrue(ValidationUtil.isValidName("Rohit"))
    }

    @Test
    fun `single character name is invalid`() {
        assertFalse(ValidationUtil.isValidName("R"))
    }

    @Test
    fun `blank name is invalid`() {
        assertFalse(ValidationUtil.isValidName("  "))
    }

    // --- Cost validation ---

    @Test
    fun `positive cost is valid`() {
        assertTrue(ValidationUtil.isValidCost("2500.50"))
    }

    @Test
    fun `zero cost is invalid`() {
        assertFalse(ValidationUtil.isValidCost("0"))
    }

    @Test
    fun `negative cost is invalid`() {
        assertFalse(ValidationUtil.isValidCost("-100"))
    }

    @Test
    fun `non-numeric cost is invalid`() {
        assertFalse(ValidationUtil.isValidCost("abc"))
    }

    // --- Phone cleaning ---

    @Test
    fun `cleanPhone strips country code and spaces`() {
        assertEquals("9876543210", ValidationUtil.cleanPhone("+91 9876543210"))
    }
}
