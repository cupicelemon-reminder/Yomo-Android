package com.binye.yomo.data.repository

import com.binye.yomo.data.model.Reminder
import com.binye.yomo.data.model.ReminderStatus
import com.binye.yomo.data.model.RecurrenceRule
import com.binye.yomo.data.model.RecurrenceType
import com.binye.yomo.data.model.RecurrenceUnit
import com.binye.yomo.data.recurrence.RecurrenceCalculator
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String?
        get() = auth.currentUser?.uid

    private fun remindersRef() = userId?.let {
        db.collection("users").document(it).collection("reminders")
    }

    fun observeReminders(): Flow<List<Reminder>> = callbackFlow {
        val ref = remindersRef()
        if (ref == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = ref
            .orderBy("triggerDate")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reminders = (snapshot?.documents ?: emptyList())
                    .mapNotNull { doc -> decodeReminder(doc.id, doc.data) }
                    // Avoid requiring a composite index (status + triggerDate) in MVP.
                    .filter { it.status == ReminderStatus.ACTIVE }
                trySend(reminders)
            }

        awaitClose { listener.remove() }
    }

    suspend fun createReminder(reminder: Reminder): Reminder {
        val ref = remindersRef() ?: throw IllegalStateException("Not authenticated")
        // Create doc ID first so we can schedule local notifications immediately.
        val doc = ref.document()
        val id = doc.id
        val created = reminder.copy(id = id, updatedAt = Timestamp.now())
        val data = encodeReminder(created)
        doc.set(data).await()
        return created
    }

    suspend fun updateReminder(reminder: Reminder) {
        val ref = remindersRef() ?: throw IllegalStateException("Not authenticated")
        if (reminder.id.isBlank()) throw IllegalArgumentException("Reminder ID required")
        val data = encodeReminder(reminder).toMutableMap()
        data["updatedAt"] = Timestamp.now()
        ref.document(reminder.id).update(data).await()
    }

    suspend fun completeReminder(reminder: Reminder) {
        val ref = remindersRef() ?: throw IllegalStateException("Not authenticated")
        if (reminder.id.isBlank()) return

        val recurrence = reminder.recurrence
        if (recurrence != null && recurrence.type != RecurrenceType.NONE) {
            val nextDate = RecurrenceCalculator.nextTriggerDate(reminder.triggerDate.toDate(), recurrence)
            ref.document(reminder.id).update(
                mapOf(
                    "triggerDate" to Timestamp(nextDate),
                    "snoozedUntil" to FieldValue.delete(),
                    "updatedAt" to Timestamp.now()
                )
            ).await()
        } else {
            ref.document(reminder.id).update(
                mapOf(
                    "status" to ReminderStatus.COMPLETED.value,
                    "completedAt" to Timestamp.now(),
                    "snoozedUntil" to FieldValue.delete(),
                    "updatedAt" to Timestamp.now()
                )
            ).await()
        }
    }

    suspend fun deleteReminder(reminderId: String) {
        val ref = remindersRef() ?: throw IllegalStateException("Not authenticated")
        ref.document(reminderId).delete().await()
    }

    suspend fun snoozeReminder(reminderId: String, until: Date) {
        val ref = remindersRef() ?: throw IllegalStateException("Not authenticated")
        ref.document(reminderId).update(
            mapOf(
                "snoozedUntil" to Timestamp(until),
                "updatedAt" to Timestamp.now()
            )
        ).await()
    }

    private fun encodeReminder(reminder: Reminder): Map<String, Any?> {
        val data = mutableMapOf<String, Any?>(
            "title" to reminder.title,
            "notes" to (reminder.notes ?: ""),
            "triggerDate" to reminder.triggerDate,
            "recurrence" to encodeRecurrence(reminder.recurrence),
            "status" to reminder.status.value,
            "completedAt" to reminder.completedAt,
            "createdAt" to reminder.createdAt,
            "updatedAt" to Timestamp.now()
        )
        reminder.snoozedUntil?.let { data["snoozedUntil"] = it }
        return data
    }

    private fun encodeRecurrence(rule: RecurrenceRule?): Map<String, Any> {
        if (rule == null) return mapOf("type" to "none")
        val data = mutableMapOf<String, Any>(
            "type" to rule.type.value,
            "interval" to rule.interval,
            "basedOnCompletion" to rule.basedOnCompletion
        )
        rule.unit?.let { data["unit"] = it.value }
        rule.daysOfWeek?.let { data["daysOfWeek"] = it }
        rule.monthOrdinal?.let { data["monthOrdinal"] = it }
        rule.monthDay?.let { data["monthDay"] = it }
        rule.timeRangeStart?.let { data["timeRangeStart"] = it }
        rule.timeRangeEnd?.let { data["timeRangeEnd"] = it }
        return data
    }

    private fun decodeReminder(id: String, data: Map<String, Any>?): Reminder? {
        if (data == null) return null
        val title = data["title"] as? String ?: return null
        val triggerDate = data["triggerDate"] as? Timestamp ?: return null
        val statusRaw = data["status"] as? String ?: return null

        val recurrenceData = data["recurrence"] as? Map<*, *>
        val recurrence = decodeRecurrence(recurrenceData)

        return Reminder(
            id = id,
            title = title,
            notes = data["notes"] as? String,
            triggerDate = triggerDate,
            recurrence = recurrence,
            status = ReminderStatus.fromValue(statusRaw),
            snoozedUntil = data["snoozedUntil"] as? Timestamp,
            completedAt = data["completedAt"] as? Timestamp,
            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
            updatedAt = data["updatedAt"] as? Timestamp ?: Timestamp.now()
        )
    }

    private fun decodeRecurrence(data: Map<*, *>?): RecurrenceRule? {
        if (data == null) return null
        val typeRaw = data["type"] as? String ?: return null
        val type = RecurrenceType.fromValue(typeRaw)
        if (type == RecurrenceType.NONE) return null

        return RecurrenceRule(
            type = type,
            interval = (data["interval"] as? Number)?.toInt() ?: 1,
            unit = (data["unit"] as? String)?.let { RecurrenceUnit.fromValue(it) },
            daysOfWeek = (data["daysOfWeek"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() },
            monthOrdinal = (data["monthOrdinal"] as? Number)?.toInt(),
            monthDay = (data["monthDay"] as? Number)?.toInt(),
            timeRangeStart = data["timeRangeStart"] as? String,
            timeRangeEnd = data["timeRangeEnd"] as? String,
            basedOnCompletion = data["basedOnCompletion"] as? Boolean ?: false
        )
    }

    private fun calculateNextDate(from: Date, recurrence: RecurrenceRule): Date {
        return RecurrenceCalculator.nextTriggerDate(from, recurrence)
    }
}
