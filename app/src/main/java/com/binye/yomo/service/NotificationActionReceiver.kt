package com.binye.yomo.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import java.util.Calendar

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(NotificationService.EXTRA_REMINDER_ID) ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val reminderRef = db.collection("users").document(userId)
            .collection("reminders").document(reminderId)

        // Dismiss notification
        val notificationService = NotificationService(context)
        notificationService.cancelNotification(reminderId)

        when (intent.action) {
            NotificationService.ACTION_SNOOZE -> {
                val snoozeUntil = Calendar.getInstance().apply {
                    add(Calendar.MINUTE, NotificationService.SNOOZE_MINUTES)
                }.time
                // Use merge set so it works even if the document was deleted and the alarm fires late.
                reminderRef.set(
                    mapOf(
                        "triggerDate" to Timestamp(snoozeUntil),
                        "snoozedUntil" to Timestamp(snoozeUntil),
                        "updatedAt" to Timestamp.now()
                    ),
                    SetOptions.merge()
                )
                // Reschedule notification for snooze time
                val title = intent.getStringExtra(NotificationService.EXTRA_REMINDER_TITLE) ?: ""
                notificationService.scheduleNotification(
                    com.binye.yomo.data.model.Reminder(
                        id = reminderId,
                        title = title,
                        snoozedUntil = Timestamp(snoozeUntil)
                    )
                )
            }
            NotificationService.ACTION_COMPLETE -> {
                // Determine if this reminder is recurring; if so, roll it forward.
                reminderRef.get().addOnSuccessListener { snap ->
                    val data = snap.data
                    val recurrenceData = data?.get("recurrence") as? Map<*, *>
                    val typeRaw = recurrenceData?.get("type") as? String
                    val isRecurring = typeRaw != null && typeRaw != "none"

                    if (!isRecurring) {
                        reminderRef.update(
                            mapOf(
                                "status" to "completed",
                                "completedAt" to Timestamp.now(),
                                "snoozedUntil" to FieldValue.delete(),
                                "updatedAt" to Timestamp.now()
                            )
                        )
                        return@addOnSuccessListener
                    }

                    // Recurring: compute next trigger date.
                    try {
                        val rule = com.binye.yomo.data.model.RecurrenceRule(
                            type = com.binye.yomo.data.model.RecurrenceType.fromValue(typeRaw ?: "none"),
                            interval = (recurrenceData?.get("interval") as? Number)?.toInt() ?: 1,
                            unit = (recurrenceData?.get("unit") as? String)?.let { com.binye.yomo.data.model.RecurrenceUnit.fromValue(it) },
                            daysOfWeek = (recurrenceData?.get("daysOfWeek") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() },
                            monthOrdinal = (recurrenceData?.get("monthOrdinal") as? Number)?.toInt(),
                            monthDay = (recurrenceData?.get("monthDay") as? Number)?.toInt(),
                            timeRangeStart = recurrenceData?.get("timeRangeStart") as? String,
                            timeRangeEnd = recurrenceData?.get("timeRangeEnd") as? String,
                            basedOnCompletion = recurrenceData?.get("basedOnCompletion") as? Boolean ?: false
                        )

                        val fromTs = (data["triggerDate"] as? Timestamp) ?: Timestamp.now()
                        val next = com.binye.yomo.data.recurrence.RecurrenceCalculator
                            .nextTriggerDate(fromTs.toDate(), rule)

                        reminderRef.update(
                            mapOf(
                                "triggerDate" to Timestamp(next),
                                "snoozedUntil" to FieldValue.delete(),
                                "updatedAt" to Timestamp.now()
                            )
                        )

                        val title = data["title"] as? String ?: ""
                        notificationService.scheduleNotification(
                            com.binye.yomo.data.model.Reminder(
                                id = reminderId,
                                title = title,
                                triggerDate = Timestamp(next)
                            )
                        )
                    } catch (_: Exception) {
                        // Fallback to non-recurring complete.
                        reminderRef.update(
                            mapOf(
                                "status" to "completed",
                                "completedAt" to Timestamp.now(),
                                "snoozedUntil" to FieldValue.delete(),
                                "updatedAt" to Timestamp.now()
                            )
                        )
                    }
                }
            }
        }
    }
}
