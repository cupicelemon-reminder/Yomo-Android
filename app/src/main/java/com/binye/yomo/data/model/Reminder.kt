package com.binye.yomo.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Reminder(
    @DocumentId val id: String = "",
    val title: String = "",
    val notes: String? = null,
    val triggerDate: Timestamp = Timestamp.now(),
    val recurrence: RecurrenceRule? = null,
    val status: ReminderStatus = ReminderStatus.ACTIVE,
    val snoozedUntil: Timestamp? = null,
    val completedAt: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    val displayDate: java.util.Date
        get() = snoozedUntil?.toDate() ?: triggerDate.toDate()

    val isOverdue: Boolean
        get() = status == ReminderStatus.ACTIVE && displayDate.before(java.util.Date())

    val isToday: Boolean
        get() {
            val cal = java.util.Calendar.getInstance()
            val today = cal.clone() as java.util.Calendar
            cal.time = displayDate
            return cal.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                cal.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)
        }

    val isTomorrow: Boolean
        get() {
            val cal = java.util.Calendar.getInstance()
            val tomorrow = cal.clone() as java.util.Calendar
            tomorrow.add(java.util.Calendar.DAY_OF_YEAR, 1)
            cal.time = displayDate
            return cal.get(java.util.Calendar.YEAR) == tomorrow.get(java.util.Calendar.YEAR) &&
                cal.get(java.util.Calendar.DAY_OF_YEAR) == tomorrow.get(java.util.Calendar.DAY_OF_YEAR)
        }
}

enum class ReminderStatus(val value: String) {
    ACTIVE("active"),
    COMPLETED("completed");

    companion object {
        fun fromValue(value: String): ReminderStatus =
            entries.firstOrNull { it.value == value } ?: ACTIVE
    }
}

data class RecurrenceRule(
    val type: RecurrenceType = RecurrenceType.NONE,
    val interval: Int = 1,
    val unit: RecurrenceUnit? = null,
    val daysOfWeek: List<Int>? = null,
    val monthOrdinal: Int? = null,
    val monthDay: Int? = null,
    val timeRangeStart: String? = null, // "HH:mm"
    val timeRangeEnd: String? = null,   // "HH:mm"
    val basedOnCompletion: Boolean = false
) {
    companion object {
        fun daily(): RecurrenceRule = RecurrenceRule(
            type = RecurrenceType.DAILY,
            interval = 1,
            unit = RecurrenceUnit.DAY
        )

        fun weekly(days: List<Int> = emptyList()): RecurrenceRule = RecurrenceRule(
            type = RecurrenceType.WEEKLY,
            interval = 1,
            unit = RecurrenceUnit.WEEK,
            daysOfWeek = days.ifEmpty { null }
        )
    }
}

enum class RecurrenceType(val value: String) {
    NONE("none"),
    DAILY("daily"),
    WEEKLY("weekly"),
    CUSTOM("custom");

    companion object {
        fun fromValue(value: String): RecurrenceType =
            entries.firstOrNull { it.value == value } ?: NONE
    }
}

enum class RecurrenceUnit(val value: String) {
    HOUR("hour"),
    DAY("day"),
    WEEK("week"),
    MONTH("month");

    companion object {
        fun fromValue(value: String): RecurrenceUnit? =
            entries.firstOrNull { it.value == value }
    }
}
