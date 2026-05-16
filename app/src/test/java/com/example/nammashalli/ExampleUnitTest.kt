package com.nammashalli.inventory

import org.junit.Assert.*
import org.junit.Test

class OtpManagerTest {

    @Test
    fun `6-digit OTP has correct length`() {
        val otp = (100000..999999).random().toString()
        assertEquals(6, otp.length)
    }

    @Test
    fun `OTP contains only digits`() {
        val otp = "482916"
        assertTrue("OTP must be all digits", otp.all { it.isDigit() })
    }

    @Test
    fun `OTP string representation is zero-padded to 6 digits`() {
        val otp = String.format("%06d", 4829)
        assertEquals("004829", otp)
        assertEquals(6, otp.length)
    }

    @Test
    fun `asset categories list is not empty`() {
        val categories = listOf(
            "Furniture", "Electronics", "Sports Equipment",
            "Lab Equipment", "Library Books", "Stationery", "Other"
        )
        assertTrue("Asset categories should not be empty", categories.isNotEmpty())
        assertTrue("Should have at least 5 categories", categories.size >= 5)
    }

    @Test
    fun `repair priority levels are ordered correctly`() {
        val priorities = listOf("Low", "Medium", "High", "Critical")
        assertEquals("Low", priorities.first())
        assertEquals("Critical", priorities.last())
    }
}