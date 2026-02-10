package com.binye.yomo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.binye.yomo.data.model.Reminder
import com.binye.yomo.data.model.RecurrenceType
import com.binye.yomo.ui.theme.CornerRadius
import com.binye.yomo.ui.theme.Elevation
import com.binye.yomo.ui.theme.Spacing
import com.binye.yomo.ui.theme.YomoColors
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ReminderCard(
    reminder: Reminder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(CornerRadius.large)
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(Elevation.card, shape)
            .clip(shape)
            .background(YomoColors.CardGlass)
            .border(0.5.dp, YomoColors.CardGlassBorder, shape)
            .clickable(onClick = onClick)
            .padding(Spacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (reminder.isOverdue) YomoColors.OverdueRed
                        else YomoColors.BrandBlue
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = YomoColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = if (reminder.isToday) {
                            timeFormat.format(reminder.displayDate)
                        } else {
                            "${dateFormat.format(reminder.displayDate)} ${timeFormat.format(reminder.displayDate)}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (reminder.isOverdue) YomoColors.OverdueRed
                        else YomoColors.TextTertiary
                    )

                    if (reminder.recurrence != null && reminder.recurrence.type != RecurrenceType.NONE) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Recurring",
                            modifier = Modifier.size(14.dp),
                            tint = YomoColors.TextTertiary
                        )
                    }
                }
            }
        }
    }
}
