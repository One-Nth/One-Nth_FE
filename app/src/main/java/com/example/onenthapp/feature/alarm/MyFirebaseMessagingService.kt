package com.example.onenthapp.feature.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.onenthapp.MainActivity
import com.example.onenthapp.R
import com.example.onenthapp.data.alarm.AlarmRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val CHANNEL_ID = "onenthapp_fcm_channel"
    private val CHANNEL_NAME = "OneNthApp Notifications"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "새 FCM 토큰 생성됨: $token")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = AlarmRepository()
                val response = repository.registerFcmToken(token)
                if (response.isSuccessful) {
                    Log.d("FCM", "토큰 등록 성공: ${response.body()?.message}")
                } else {
                    Log.e("FCM", "토큰 등록 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("FCM", "토큰 등록 중 예외 발생: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "알림 수신: ${remoteMessage.data}")

        // Notification 채널 생성 (한 번만 생성됨)
        createNotificationChannel()

        // 데이터 메시지에서 알림 내용 꺼내기
        val title = remoteMessage.data["title"] ?: "알림"
        val message = remoteMessage.data["value"] ?: "새 알림이 도착했습니다."

        sendNotification(title, message)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "OneNthApp FCM 알림 채널"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 앱 메인 액티비티로 이동하는 인텐트
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_main_logo)  // 적절한 아이콘 리소스로 교체
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
