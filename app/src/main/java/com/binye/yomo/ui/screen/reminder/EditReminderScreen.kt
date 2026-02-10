package com.binye.yomo.ui.screen.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.binye.yomo.data.model.RecurrenceType
import com.binye.yomo.ui.component.FormField
import com.binye.yomo.ui.component.GlassCard
import com.binye.yomo.ui.component.PillButton
import com.binye.yomo.ui.component.PrimaryButton
import com.binye.yomo.ui.theme.CornerRadius
import com.binye.yomo.ui.theme.Spacing
import com.binye.yomo.ui.theme.YomoColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderScreen(
    reminderId: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: EditReminderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    LaunchedEffect(reminderId) {
        viewModel.loadReminder(reminderId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YomoColors.Background)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = YomoColors.BrandBlue
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Edit Reminder", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = YomoColors.TextPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.deleteReminder(onDeleted) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = YomoColors.OverdueRed
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = YomoColors.Background,
                        titleContentColor = YomoColors.TextPrimary
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.md)
                ) {
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    FormField(
                        value = state.title,
                        onValueChange = viewModel::updateTitle,
                        label = "Title"
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    FormField(
                        value = state.notes,
                        onValueChange = viewModel::updateNotes,
                        label = "Notes (optional)",
                        singleLine = false,
                        maxLines = 4,
                        minHeight = 80
                    )

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    Text(
                        text = "When",
                        style = MaterialTheme.typography.labelLarge,
                        color = YomoColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            cornerRadius = CornerRadius.medium
                        ) {
                            TextButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = dateFormat.format(state.date),
                                    color = YomoColors.TextPrimary
                                )
                            }
                        }

                        GlassCard(
                            modifier = Modifier.weight(1f),
                            cornerRadius = CornerRadius.medium
                        ) {
                            TextButton(
                                onClick = { showTimePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = timeFormat.format(state.date),
                                    color = YomoColors.TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    Text(
                        text = "Repeat",
                        style = MaterialTheme.typography.labelLarge,
                        color = YomoColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        RecurrenceType.entries
                            .filter { it != RecurrenceType.CUSTOM }
                            .forEach { type ->
                                PillButton(
                                    text = when (type) {
                                        RecurrenceType.NONE -> "None"
                                        RecurrenceType.DAILY -> "Daily"
                                        RecurrenceType.WEEKLY -> "Weekly"
                                        RecurrenceType.CUSTOM -> "Custom"
                                    },
                                    selected = state.recurrenceType == type,
                                    onClick = { viewModel.updateRecurrence(type) }
                                )
                            }
                    }

                    if (state.recurrenceType == RecurrenceType.CUSTOM) {
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        GlassCard(cornerRadius = CornerRadius.large) {
                            Text(
                                text = "Custom recurrence was created on another platform.\n" +
                                    "Editing it is disabled on Android to avoid data loss in MVP.",
                                color = YomoColors.TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (state.recurrenceType == RecurrenceType.WEEKLY) {
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        WeekdaySelector(
                            selected = state.selectedWeekDays,
                            onToggle = viewModel::toggleWeekDay
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    state.error?.let { error ->
                        Text(
                            text = error,
                            color = YomoColors.OverdueRed,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                    }

                    PrimaryButton(
                        text = "Save Changes",
                        onClick = { viewModel.saveReminder(onSaved) },
                        isLoading = state.isSaving
                    )

                    Spacer(modifier = Modifier.height(Spacing.xl))
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.date.time)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selected = Calendar.getInstance().apply { timeInMillis = millis }
                        val current = Calendar.getInstance().apply { time = state.date }
                        selected.set(Calendar.HOUR_OF_DAY, current.get(Calendar.HOUR_OF_DAY))
                        selected.set(Calendar.MINUTE, current.get(Calendar.MINUTE))
                        viewModel.updateDate(selected.time)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { time = state.date }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            GlassCard(cornerRadius = CornerRadius.large) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(Spacing.md)
                ) {
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel", color = YomoColors.TextSecondary)
                        }
                        TextButton(onClick = {
                            val updated = Calendar.getInstance().apply {
                                time = state.date
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                            }
                            viewModel.updateDate(updated.time)
                            showTimePicker = false
                        }) { Text("OK", color = YomoColors.BrandBlue) }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekdaySelector(
    selected: Set<Int>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf(
        Calendar.MONDAY to "Mon",
        Calendar.TUESDAY to "Tue",
        Calendar.WEDNESDAY to "Wed",
        Calendar.THURSDAY to "Thu",
        Calendar.FRIDAY to "Fri",
        Calendar.SATURDAY to "Sat",
        Calendar.SUNDAY to "Sun"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        days.forEach { (dayInt, label) ->
            PillButton(
                text = label,
                selected = selected.contains(dayInt),
                onClick = { onToggle(dayInt) }
            )
        }
    }
}
