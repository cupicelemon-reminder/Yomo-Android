package com.binye.yomo.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.os.Build

class YomoFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        registerToken(userId, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val action = data["action"] ?: return
        val reminderId = data["reminderId"] ?: return

        val notificationService = NotificationService(applicationContext)

        when (action) {
            "completed", "deleted" -> {
                notificationService.cancelNotification(reminderId)
            }
            "snoozed" -> {
                notificationService.cancelNotification(reminderId)
                val title = data["title"] ?: return
                val newTriggerDate = data["newTriggerDate"] ?: return
                try {
                    val date = java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        java.util.Locale.US
                    ).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }.parse(newTriggerDate) ?: return

                    notificationService.scheduleNotification(
                        com.binye.yomo.data.model.Reminder(
                            id = reminderId,
                            title = title,
                            snoozedUntil = Timestamp(date)
                        )
                    )
                } catch (_: Exception) {
                    // Parse error — skip rescheduling
                }
            }
            "created", "updated" -> {
                val title = data["title"] ?: return
                val triggerDate = data["triggerDate"] ?: return
                try {
                    val date = java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        java.util.Locale.US
                    ).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }.parse(triggerDate) ?: return

                    notificationService.scheduleNotification(
                        com.binye.yomo.data.model.Reminder(
                            id = reminderId,
                            title = title,
                            triggerDate = Timestamp(date)
                        )
                    )
                } catch (_: Exception) {
                    // Parse error
                }
            }
        }
    }

    private fun registerToken(userId: String, token: String) {
        val db = FirebaseFirestore.getInstance()
        val deviceId = getAndroidDeviceId()
        val deviceRef = db.collection("users").document(userId)
            .collection("devices").document(deviceId)

        val deviceData = mapOf(
            "fcmToken" to token,
            "platform" to "android",
            "deviceName" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "lastActiveAt" to Timestamp.now(),
            "appVersion" to try {
                packageManager.getPackageInfo(packageName, 0).versionName
            } catch (_: Exception) {
                "1.0"
            }
        )

        deviceRef.set(deviceData, com.google.firebase.firestore.SetOptions.merge())
    }

    private fun getAndroidDeviceId(): String {
        return android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: java.util.UUID.randomUUID().toString()
    }
}
