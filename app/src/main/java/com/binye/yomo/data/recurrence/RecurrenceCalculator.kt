package com.binye.yomo.data.recurrence

import com.binye.yomo.data.model.RecurrenceRule
import com.binye.yomo.data.model.RecurrenceType
import com.binye.yomo.data.model.RecurrenceUnit
import java.util.Calendar
import java.util.Date

/**
 * Shared recurrence calculation used by repository + notification receivers.
 *
 * Rules (MVP):
 * - DAILY: every N days
 * - WEEKLY: every N weeks, optionally restricted to specific weekdays (Calendar.DAY_OF_WEEK ints)
 * - CUSTOM: uses [RecurrenceUnit] with interval N (weekday/month rules not implemented in MVP UI)
 */
object RecurrenceCalculator {
    fun nextTriggerDate(from: Date, recurrence: RecurrenceRule, now: Date = Date()): Date {
        return when (recurrence.type) {
            RecurrenceType.DAILY -> nextByCalendarField(from, now, Calendar.DAY_OF_YEAR, recurrence.interval)
            RecurrenceType.WEEKLY -> nextWeekly(from, now, recurrence.interval, recurrence.daysOfWeek)
            RecurrenceType.CUSTOM -> {
                val field = when (recurrence.unit) {
                    RecurrenceUnit.HOUR -> Calendar.HOUR_OF_DAY
                    RecurrenceUnit.DAY -> Calendar.DAY_OF_YEAR
                    RecurrenceUnit.WEEK -> Calendar.WEEK_OF_YEAR
                    RecurrenceUnit.MONTH -> Calendar.MONTH
                    null -> Calendar.DAY_OF_YEAR
                }
                nextByCalendarField(from, now, field, recurrence.interval)
            }
            RecurrenceType.NONE -> from
        }
    }

    private fun nextByCalendarField(from: Date, now: Date, field: Int, interval: Int): Date {
        val cal = Calendar.getInstance().apply { time = from }
        while (cal.time <= now) {
            cal.add(field, interval.coerceAtLeast(1))
        }
        return cal.time
    }

    private fun nextWeekly(from: Date, now: Date, intervalWeeks: Int, daysOfWeek: List<Int>?): Date {
        val targetDays = daysOfWeek?.distinct()?.filter { it in 1..7 }?.sorted()
        if (targetDays.isNullOrEmpty()) {
            // Simple weekly repeat (every N weeks).
            return nextByCalendarField(from, now, Calendar.WEEK_OF_YEAR, intervalWeeks)
        }

        // Keep the original hour/min/sec for the next occurrence.
        val base = Calendar.getInstance().apply { time = from }
        val hour = base.get(Calendar.HOUR_OF_DAY)
        val minute = base.get(Calendar.MINUTE)
        val second = base.get(Calendar.SECOND)
        val ms = base.get(Calendar.MILLISECOND)

        // Start searching from "now" (not from 'from'), but keep the time-of-day from 'from'.
        val cursor = Calendar.getInstance().apply {
            time = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, ms)
        }

        // If the next occurrence for "today" at that time is already past, move to next day.
        if (cursor.time <= now) {
            cursor.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Search up to (intervalWeeks*7 + 7) days to find a matching weekday.
        // This is deterministic and fast for MVP.
        val maxDays = intervalWeeks.coerceAtLeast(1) * 7 + 7
        for (i in 0..maxDays) {
            val day = cursor.get(Calendar.DAY_OF_WEEK)
            if (day in targetDays) {
                // Ensure we don't return earlier than 'from' if 'from' is in the future.
                val candidate = cursor.time
                return if (candidate < from) from else candidate
            }
            cursor.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Fallback: every N weeks.
        return nextByCalendarField(from, now, Calendar.WEEK_OF_YEAR, intervalWeeks)
    }
}

