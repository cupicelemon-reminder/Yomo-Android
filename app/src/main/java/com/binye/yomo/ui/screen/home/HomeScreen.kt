package com.binye.yomo.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.binye.yomo.R
import com.binye.yomo.data.model.Reminder
import com.binye.yomo.ui.component.PillButton
import com.binye.yomo.ui.component.ReminderCard
import com.binye.yomo.ui.theme.Spacing
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
        containerColor = YomoColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.yomo_logo),
                        contentDescription = "Yomo",
                        modifier = Modifier.size(36.dp)
                    )
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = YomoColors.TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = YomoColors.Background,
                    titleContentColor = YomoColors.TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateReminder,
                containerColor = YomoColors.BrandBlue,
                contentColor = Color.White,
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
                .background(YomoColors.Background)
                .padding(padding)
        ) {
            if (isEmpty) {
                EmptyState(onCreateReminder = onCreateReminder)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    if (grouped.overdue.isNotEmpty()) {
                        item {
                            SectionHeader("OVERDUE", YomoColors.OverdueRed)
                        }
                        itemsIndexed(
                            grouped.overdue,
                            key = { _, r -> r.id }
                        ) { index, reminder ->
                            SwipeableReminder(
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
                            SwipeableReminder(
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
                            SwipeableReminder(
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
                            SectionHeader("UPCOMING", YomoColors.TextTertiary)
                        }
                        itemsIndexed(
                            grouped.upcoming,
                            key = { _, r -> r.id }
                        ) { index, reminder ->
                            SwipeableReminder(
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
        modifier = Modifier.padding(vertical = Spacing.sm, horizontal = Spacing.xs)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableReminder(
    reminder: Reminder,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    index: Int
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.EndToStart -> onDelete()
            SwipeToDismissBoxValue.StartToEnd -> onComplete()
            SwipeToDismissBoxValue.Settled -> {}
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
                val alignment = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.Center
                }
                val backgroundColor = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> YomoColors.SuccessGreen
                    SwipeToDismissBoxValue.EndToStart -> YomoColors.OverdueRed
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = Spacing.md),
                    contentAlignment = alignment
                ) {
                    when (dismissState.dismissDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> Icon(
                            Icons.Default.Check,
                            contentDescription = "Complete",
                            tint = Color.White
                        )
                        SwipeToDismissBoxValue.EndToStart -> Text(
                            text = "Delete",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        else -> {}
                    }
                }
            },
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true,
            content = {
                ReminderCard(
                    reminder = reminder,
                    onClick = onClick
                )
            }
        )
    }
}

@Composable
private fun EmptyState(onCreateReminder: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.yomo_logo),
            contentDescription = "Yomo",
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = "All clear! Nothing to remind you about.",
            style = MaterialTheme.typography.bodyLarge,
            color = YomoColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        PillButton(
            text = "Create a reminder",
            selected = true,
            onClick = onCreateReminder
        )
    }
}
