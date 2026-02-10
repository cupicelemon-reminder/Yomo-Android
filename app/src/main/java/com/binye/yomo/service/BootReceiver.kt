package com.binye.yomo.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val notificationService = NotificationService(context)

        db.collection("users").document(userId).collection("reminders")
            .whereEqualTo("status", "active")
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    val data = doc.data ?: continue
                    val title = data["title"] as? String ?: continue
                    val triggerDate = data["triggerDate"] as? com.google.firebase.Timestamp ?: continue
                    val snoozedUntil = data["snoozedUntil"] as? com.google.firebase.Timestamp

                    val reminder = com.binye.yomo.data.model.Reminder(
                        id = doc.id,
                        title = title,
                        triggerDate = triggerDate,
                        snoozedUntil = snoozedUntil
                    )
                    notificationService.scheduleNotification(reminder)
                }
            }
    }
}
