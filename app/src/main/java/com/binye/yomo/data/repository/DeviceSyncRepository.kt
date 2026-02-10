package com.binye.yomo.data.repository

import android.os.Build
import android.provider.Settings
import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceSyncRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val messaging: FirebaseMessaging,
    @ApplicationContext private val context: Context
) {
    suspend fun registerDevice() {
        val userId = auth.currentUser?.uid ?: return
        val token = try {
            messaging.token.await()
        } catch (_: Exception) {
            return
        }

        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: return

        val deviceData = mapOf(
            "fcmToken" to token,
            "platform" to "android",
            "deviceName" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "lastActiveAt" to Timestamp.now(),
            "appVersion" to try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (_: Exception) {
                "1.0"
            }
        )

        db.collection("users").document(userId)
            .collection("devices").document(deviceId)
            .set(deviceData, SetOptions.merge())
            .await()
    }

    suspend fun updateLastActive() {
        val userId = auth.currentUser?.uid ?: return
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: return

        db.collection("users").document(userId)
            .collection("devices").document(deviceId)
            .update("lastActiveAt", Timestamp.now())
            .await()
    }
}
