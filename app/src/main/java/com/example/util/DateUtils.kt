package com.example.util

import java.util.Calendar

object DateUtils {
    val THAI_MONTHS = listOf(
        "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน",
        "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม",
        "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
    )

    val THAI_MONTHS_SHORT = listOf(
        "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.",
        "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.",
        "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."
    )

    val THAI_WEEKDAYS_SHORT = listOf("อา", "จ", "อ", "พ", "พฤ", "ศ", "ส")

    fun toBuddhistYear(gregorianYear: Int): Int = gregorianYear + 543

    fun formatMonthYearThai(year: Int, month: Int): String {
        val monthName = if (month in 1..12) THAI_MONTHS[month - 1] else ""
        val buddhistYear = toBuddhistYear(year)
        return "$monthName $buddhistYear"
    }

    fun formatDateToThaiFull(isoDate: String): String {
        val parts = isoDate.split("-")
        if (parts.size != 3) return isoDate
        val year = parts[0].toIntOrNull() ?: return isoDate
        val month = parts[1].toIntOrNull() ?: return isoDate
        val day = parts[2].toIntOrNull() ?: return isoDate

        val monthName = if (month in 1..12) THAI_MONTHS[month - 1] else ""
        val buddhistYear = toBuddhistYear(year)
        return "$day $monthName $buddhistYear"
    }

    fun toIsoDateString(year: Int, month: Int, day: Int): String {
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    fun toYearMonthString(year: Int, month: Int): String {
        return String.format("%04d-%02d", year, month)
    }

    fun getTodayIsoString(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return toIsoDateString(year, month, day)
    }

    /**
     * Returns a list of day numbers or null for empty padding in the 7-column calendar grid
     */
    fun getMonthCalendarCells(year: Int, month: Int): List<Int?> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        // Sunday is 1, Monday is 2, ..., Saturday is 7
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val cells = mutableListOf<Int?>()
        // Leading empty slots for days before the 1st
        for (i in 1 until firstDayOfWeek) {
            cells.add(null)
        }
        // Month days
        for (day in 1..daysInMonth) {
            cells.add(day)
        }

        return cells
    }
}
