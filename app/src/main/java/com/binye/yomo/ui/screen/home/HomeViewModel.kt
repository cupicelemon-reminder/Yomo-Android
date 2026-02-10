package com.binye.yomo.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binye.yomo.data.model.Reminder
import com.binye.yomo.data.model.RecurrenceType
import com.binye.yomo.data.repository.ReminderRepository
import com.binye.yomo.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class GroupedReminders(
    val overdue: List<Reminder> = emptyList(),
    val today: List<Reminder> = emptyList(),
    val tomorrow: List<Reminder> = emptyList(),
    val upcoming: List<Reminder> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val notificationService: NotificationService
) : ViewModel() {

    val groupedReminders: StateFlow<GroupedReminders> = reminderRepository.observeReminders()
        .map { reminders -> reminders.sortedBy { it.displayDate } }
        .map { reminders -> groupReminders(reminders) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupedReminders())

    val isEmpty: StateFlow<Boolean> = reminderRepository.observeReminders()
        .map { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun completeReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                reminderRepository.completeReminder(reminder)
                // If not recurring, cancel local notification immediately.
                // Recurring notifications will be rescheduled by Firestore/FCM + next trigger.
                if (reminder.recurrence == null || reminder.recurrence.type == RecurrenceType.NONE) {
                    notificationService.cancelNotification(reminder.id)
                }
            } catch (_: Exception) {
                // Error handling — Firestore listener will reflect actual state
            }
        }
    }

    fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            try {
                reminderRepository.deleteReminder(reminderId)
                notificationService.cancelNotification(reminderId)
            } catch (_: Exception) {
                // Error handling
            }
        }
    }

    private fun groupReminders(reminders: List<Reminder>): GroupedReminders {
        val now = Calendar.getInstance()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val tomorrowStart = (todayStart.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val dayAfterTomorrow = (todayStart.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 2)
        }

        val overdue = mutableListOf<Reminder>()
        val today = mutableListOf<Reminder>()
        val tomorrow = mutableListOf<Reminder>()
        val upcoming = mutableListOf<Reminder>()

        for (reminder in reminders) {
            val displayCal = Calendar.getInstance().apply { time = reminder.displayDate }
            when {
                reminder.isOverdue -> overdue.add(reminder)
                displayCal >= todayStart && displayCal < tomorrowStart -> today.add(reminder)
                displayCal >= tomorrowStart && displayCal < dayAfterTomorrow -> tomorrow.add(reminder)
                else -> upcoming.add(reminder)
            }
        }

        return GroupedReminders(
            overdue = overdue,
            today = today,
            tomorrow = tomorrow,
            upcoming = upcoming
        )
    }
}
