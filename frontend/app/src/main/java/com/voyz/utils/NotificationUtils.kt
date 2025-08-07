package com.voyz.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationUtils {
    private const val CHANNEL_ID = "firebase_channel"
    private const val NOTIFICATION_ID = 1

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Firebase 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Firebase로부터 받는 알림"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String?, message: String?) {
        // 알림 생성 및 표시 로직
    }
}
