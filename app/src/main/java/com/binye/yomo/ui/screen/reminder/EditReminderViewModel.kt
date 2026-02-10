package com.binye.yomo.ui.screen.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binye.yomo.data.model.Reminder
import com.binye.yomo.data.model.RecurrenceRule
import com.binye.yomo.data.model.RecurrenceType
import com.binye.yomo.data.repository.ReminderRepository
import com.google.firebase.Timestamp
import com.binye.yomo.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class EditReminderState(
    val reminder: Reminder? = null,
    val title: String = "",
    val notes: String = "",
    val date: Date = Date(),
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val selectedWeekDays: Set<Int> = emptySet(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _state = MutableStateFlow(EditReminderState())
    val state: StateFlow<EditReminderState> = _state.asStateFlow()

    fun loadReminder(reminderId: String) {
        viewModelScope.launch {
            reminderRepository.observeReminders().collect { reminders ->
                val reminder = reminders.firstOrNull { it.id == reminderId }
                if (reminder != null && _state.value.reminder == null) {
                    _state.value = EditReminderState(
                        reminder = reminder,
                        title = reminder.title,
                        notes = reminder.notes ?: "",
                        date = reminder.triggerDate.toDate(),
                        recurrenceType = reminder.recurrence?.type ?: RecurrenceType.NONE,
                        selectedWeekDays = reminder.recurrence?.daysOfWeek?.toSet() ?: emptySet(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _state.value = _state.value.copy(title = title)
    }

    fun updateNotes(notes: String) {
        _state.value = _state.value.copy(notes = notes)
    }

    fun updateDate(date: Date) {
        _state.value = _state.value.copy(date = date)
    }

    fun updateRecurrence(type: RecurrenceType) {
        _state.value = _state.value.copy(recurrenceType = type)
    }

    fun toggleWeekDay(day: Int) {
        val current = _state.value
        val updated = current.selectedWeekDays.toMutableSet().apply {
            if (contains(day)) remove(day) else add(day)
        }
        _state.value = current.copy(selectedWeekDays = updated)
    }

    fun saveReminder(onSuccess: () -> Unit) {
        val current = _state.value
        val original = current.reminder ?: return
        if (current.title.isBlank()) {
            _state.value = current.copy(error = "Title is required")
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(isSaving = true, error = null)
            try {
                if (current.recurrenceType == RecurrenceType.CUSTOM) {
                    _state.value = current.copy(
                        isSaving = false,
                        error = "Custom recurrence can't be edited on Android in MVP."
                    )
                    return@launch
                }

                val recurrence = when (current.recurrenceType) {
                    RecurrenceType.NONE -> null
                    RecurrenceType.DAILY -> RecurrenceRule.daily()
                    RecurrenceType.WEEKLY -> RecurrenceRule.weekly(current.selectedWeekDays.toList())
                    RecurrenceType.CUSTOM -> null
                }

                val updated = original.copy(
                    title = current.title,
                    notes = current.notes.ifBlank { null },
                    triggerDate = Timestamp(current.date),
                    recurrence = recurrence,
                    updatedAt = Timestamp.now()
                )
                reminderRepository.updateReminder(updated)
                notificationService.cancelNotification(updated.id)
                notificationService.scheduleNotification(updated)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save"
                )
            }
        }
    }

    fun deleteReminder(onSuccess: () -> Unit) {
        val reminderId = _state.value.reminder?.id ?: return
        viewModelScope.launch {
            try {
                reminderRepository.deleteReminder(reminderId)
                notificationService.cancelNotification(reminderId)
                onSuccess()
            } catch (_: Exception) {
                // Error handled by Firestore listener
            }
        }
    }
}
