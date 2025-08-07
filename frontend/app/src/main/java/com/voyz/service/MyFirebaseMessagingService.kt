package com.voyz.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import androidx.core.app.NotificationCompat
import com.voyz.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 포그라운드에서 메시지 수신 시 처리
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }

        // 데이터 메시지 처리
        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "새로운 토큰: $token")
        // 서버로 토큰 전송하는 로직 추가
        sendTokenToServer(token)
    }

    private fun showNotification(title: String?, body: String?) {
        val channelId = "fcm_channel"
        val notificationId = System.currentTimeMillis().toInt()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 이상에서 알림 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "FCM 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title ?: "알림")
            .setContentText(body ?: "새 메시지가 있습니다")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        // 데이터 메시지 처리 로직
    }

    private fun sendTokenToServer(token: String) {
        // 서버로 토큰 전송 로직
    }
}