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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.binye.yomo.data.model.RecurrenceType
import com.binye.yomo.ui.component.GlassCard
import com.binye.yomo.ui.theme.YomoColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: CreateReminderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = YomoColors.BrandBlue,
        unfocusedBorderColor = YomoColors.CardGlassBorder,
        focusedTextColor = YomoColors.TextPrimary,
        unfocusedTextColor = YomoColors.TextPrimary,
        cursorColor = YomoColors.BrandBlue,
        focusedLabelColor = YomoColors.BrandBlue,
        unfocusedLabelColor = YomoColors.TextMuted
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(YomoColors.BackgroundStart, YomoColors.BackgroundEnd),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("New Reminder", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = YomoColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = YomoColors.TextPrimary
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Title
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::updateTitle,
                    label = { Text("What do you need to remember?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notes
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::updateNotes,
                    label = { Text("Notes (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Date & Time
                Text(
                    text = "When",
                    style = MaterialTheme.typography.labelLarge,
                    color = YomoColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlassCard(
                        modifier = Modifier
                            .weight(1f),
                        cornerRadius = 12.dp
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
                        modifier = Modifier
                            .weight(1f),
                        cornerRadius = 12.dp
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

                Spacer(modifier = Modifier.height(24.dp))

                // Recurrence
                Text(
                    text = "Repeat",
                    style = MaterialTheme.typography.labelLarge,
                    color = YomoColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecurrenceType.entries
                        .filter { it != RecurrenceType.CUSTOM }
                        .forEach { type ->
                            FilterChip(
                                selected = state.recurrenceType == type,
                                onClick = { viewModel.updateRecurrence(type) },
                                label = {
                                    Text(
                                        when (type) {
                                            RecurrenceType.NONE -> "None"
                                            RecurrenceType.DAILY -> "Daily"
                                            RecurrenceType.WEEKLY -> "Weekly"
                                            RecurrenceType.CUSTOM -> "Custom"
                                        }
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = YomoColors.BrandBlue,
                                    selectedLabelColor = YomoColors.TextPrimary,
                                    containerColor = YomoColors.CardGlass,
                                    labelColor = YomoColors.TextSecondary
                                )
                            )
                        }
                }

                if (state.recurrenceType == RecurrenceType.WEEKLY) {
                    Spacer(modifier = Modifier.height(12.dp))
                    WeekdaySelector(
                        selected = state.selectedWeekDays,
                        onToggle = viewModel::toggleWeekDay
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Error
                state.error?.let { error ->
                    Text(
                        text = error,
                        color = YomoColors.OverdueRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Create button
                Button(
                    onClick = { viewModel.createReminder(onCreated) },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = YomoColors.BrandBlue)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = YomoColors.TextPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Create Reminder",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date.time
        )
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
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { time = state.date }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            GlassCard(cornerRadius = 24.dp) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
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
                        }) {
                            Text("OK", color = YomoColors.BrandBlue)
                        }
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
            FilterChip(
                selected = selected.contains(dayInt),
                onClick = { onToggle(dayInt) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = YomoColors.BrandBlue,
                    selectedLabelColor = YomoColors.TextPrimary,
                    containerColor = YomoColors.CardGlass,
                    labelColor = YomoColors.TextSecondary
                )
            )
        }
    }
}
