package com.binye.yomo.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(NotificationService.EXTRA_REMINDER_ID) ?: return
        val title = intent.getStringExtra(NotificationService.EXTRA_REMINDER_TITLE) ?: return

        val notificationService = NotificationService(context)
        notificationService.showNotification(reminderId, title)
    }
}
