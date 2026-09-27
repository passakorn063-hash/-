package com.example

import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testBuddhistYearConversion() {
        val gregorianYear = 2026
        val buddhistYear = DateUtils.toBuddhistYear(gregorianYear)
        assertEquals(2569, buddhistYear)
    }

    @Test
    fun testThaiMonthFormatting() {
        val formatted = DateUtils.formatMonthYearThai(2026, 9)
        assertEquals("กันยายน 2569", formatted)
    }

    @Test
    fun testThaiFullDateFormatting() {
        val formatted = DateUtils.formatDateToThaiFull("2026-09-28")
        assertEquals("28 กันยายน 2569", formatted)
    }

    @Test
    fun testMonthCalendarCells() {
        val cells = DateUtils.getMonthCalendarCells(2026, 9)
        assertTrue(cells.isNotEmpty())
        assertTrue(cells.contains(1))
        assertTrue(cells.contains(28))
        assertTrue(cells.contains(30))
    }
}
