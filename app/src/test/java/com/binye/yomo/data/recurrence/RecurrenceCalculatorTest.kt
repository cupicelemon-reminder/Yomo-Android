package com.binye.yomo.data.recurrence

import com.binye.yomo.data.model.RecurrenceRule
import com.binye.yomo.data.model.RecurrenceType
import com.binye.yomo.data.model.RecurrenceUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class RecurrenceCalculatorTest {

    @Test
    fun weekly_tue_thu_from_wed_next_is_thu_same_time() {
        val now = cal(2026, Calendar.FEBRUARY, 11, 10, 0) // Wed
        val from = cal(2026, Calendar.FEBRUARY, 10, 9, 30) // Tue 09:30 (original time)

        val rule = RecurrenceRule(
            type = RecurrenceType.WEEKLY,
            interval = 1,
            unit = RecurrenceUnit.WEEK,
            daysOfWeek = listOf(Calendar.TUESDAY, Calendar.THURSDAY)
        )

        val next = RecurrenceCalculator.nextTriggerDate(from, rule, now)
        val expected = cal(2026, Calendar.FEBRUARY, 12, 9, 30) // Thu 09:30
        assertEquals(expected.time, next.time)
    }

    @Test
    fun weekly_mon_from_mon_after_time_next_is_next_week_mon_same_time() {
        val now = cal(2026, Calendar.FEBRUARY, 9, 12, 0) // Mon 12:00
        val from = cal(2026, Calendar.FEBRUARY, 9, 9, 0) // Mon 09:00

        val rule = RecurrenceRule(
            type = RecurrenceType.WEEKLY,
            interval = 1,
            unit = RecurrenceUnit.WEEK,
            daysOfWeek = listOf(Calendar.MONDAY)
        )

        val next = RecurrenceCalculator.nextTriggerDate(from, rule, now)
        val expected = cal(2026, Calendar.FEBRUARY, 16, 9, 0) // next Mon 09:00
        assertEquals(expected.time, next.time)
    }

    private fun cal(year: Int, month: Int, day: Int, hour: Int, minute: Int): java.util.Date {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
}
