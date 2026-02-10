package com.binye.yomo.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.binye.yomo.data.model.Reminder
import com.binye.yomo.ui.component.ReminderCard
import com.binye.yomo.ui.theme.YomoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreateReminder: () -> Unit,
    onEditReminder: (String) -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val grouped by viewModel.groupedReminders.collectAsStateWithLifecycle()
    val isEmpty by viewModel.isEmpty.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Yomo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = YomoColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = YomoColors.TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateReminder,
                containerColor = YomoColors.BrandBlue,
                contentColor = YomoColors.TextPrimary,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "New Reminder",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
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
                .padding(padding)
        ) {
            if (isEmpty) {
                EmptyState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (grouped.overdue.isNotEmpty()) {
                        item {
                            SectionHeader("OVERDUE", YomoColors.OverdueRed)
                        }
                        itemsIndexed(
                            grouped.overdue,
                            key = { _, r -> r.id }
                        ) { index, reminder ->
                            SwipeToDeleteReminder(
                                reminder = reminder,
                                onDelete = { viewModel.deleteReminder(reminder.id) },
                                onClick = { onEditReminder(reminder.id) },
                                onComplete = { viewModel.completeReminder(reminder) },
                                index = index
                            )
                        }
                    }

                    if (grouped.today.isNotEmpty()) {
                        item {
                            SectionHeader("TODAY", YomoColors.BrandBlue)
                        }
                        itemsIndexed(
                            grouped.today,
                            key = { _, r -> r.id }
                        ) { index, reminder ->
                            SwipeToDeleteReminder(
                                reminder = reminder,
                                onDelete = { viewModel.deleteReminder(reminder.id) },
                                onClick = { onEditReminder(reminder.id) },
                                onComplete = { viewModel.completeReminder(reminder) },
                                index = index
                            )
                        }
                    }

                    if (grouped.tomorrow.isNotEmpty()) {
                        item {
                            SectionHeader("TOMORROW", YomoColors.CheckGold)
                        }
                        itemsIndexed(
                            grouped.tomorrow,
                            key = { _, r -> r.id }
                        ) { index, reminder ->
                            SwipeToDeleteReminder(
                                reminder = reminder,
                                onDelete = { viewModel.deleteReminder(reminder.id) },
                                onClick = { onEditReminder(reminder.id) },
                                onComplete = { viewModel.completeReminder(reminder) },
                                index = index
                            )
                        }
                    }

                    if (grouped.upcoming.isNotEmpty()) {
                        item {
                            SectionHeader("UPCOMING", YomoColors.TextMuted)
                        }
                        itemsIndexed(
                            grouped.upcoming,
                            key = { _, r -> r.id }
                        ) { index, reminder ->
                            SwipeToDeleteReminder(
                                reminder = reminder,
                                onDelete = { viewModel.deleteReminder(reminder.id) },
                                onClick = { onEditReminder(reminder.id) },
                                onComplete = { viewModel.completeReminder(reminder) },
                                index = index
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteReminder(
    reminder: Reminder,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    index: Int
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(300, delayMillis = index * 50)) +
            slideInVertically(tween(300, delayMillis = index * 50)) { it / 2 }
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Delete",
                        color = YomoColors.OverdueRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            enableDismissFromStartToEnd = false,
            content = {
                ReminderCard(
                    reminder = reminder,
                    onClick = onClick,
                    onComplete = onComplete
                )
            }
        )
    }
}

@Composable
private fun EmptyState() {
    val messages = listOf(
        "No reminders yet.\nTap + to create your first one!",
        "Your schedule is clear!\nEnjoy the moment.",
        "Nothing to remind you about.\nLet's add something!"
    )
    val message = messages.random()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎯",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = YomoColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
