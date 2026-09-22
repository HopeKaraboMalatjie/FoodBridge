package com.foodbridge.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.foodbridge.app.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FR-7: push notifications for a new nearby listing, a confirmed reservation, or a
 * pickup window about to close. The Cloud Function backend triggers these sends;
 * this service just renders whatever payload arrives.
 */
class FoodBridgeMessagingService : FirebaseMessagingService() {

    private val channelId = "foodbridge_alerts"

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "FoodBridge"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // In production: send the token to the REST API so the backend can target this device,
        // e.g. POST /users/{id}/fcm-token. Left as a hook for the team to wire up.
    }

    private fun showNotification(title: String, body: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(channelId, "FoodBridge alerts", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
    }
}
