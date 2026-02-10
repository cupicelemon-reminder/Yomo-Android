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
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

data class ReminderFormState(
    val title: String = "",
    val notes: String = "",
    val date: Date = Calendar.getInstance().apply {
        add(Calendar.HOUR_OF_DAY, 1)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.time,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val selectedWeekDays: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _state = MutableStateFlow(ReminderFormState())
    val state: StateFlow<ReminderFormState> = _state.asStateFlow()

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

    fun createReminder(onSuccess: () -> Unit) {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.value = current.copy(error = "Title is required")
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(isLoading = true, error = null)
            try {
                val recurrence = when (current.recurrenceType) {
                    RecurrenceType.NONE -> null
                    RecurrenceType.DAILY -> RecurrenceRule.daily()
                    RecurrenceType.WEEKLY -> RecurrenceRule.weekly(current.selectedWeekDays.toList())
                    RecurrenceType.CUSTOM -> null
                }

                val reminder = Reminder(
                    title = current.title,
                    notes = current.notes.ifBlank { null },
                    triggerDate = Timestamp(current.date),
                    recurrence = recurrence,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )
                val created = reminderRepository.createReminder(reminder)
                notificationService.scheduleNotification(created)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create reminder"
                )
            }
        }
    }
}
