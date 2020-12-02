package com.jasonmustafa.covidreminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build

/**
 * Create a notification channel for Covid reminders on start of the application.
 */
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                //"GeofenceStatus",
                "channel_01",
                NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setShowBadge(true)
        }

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.enableVibration(true)
        notificationChannel.description = "Notification description"

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

private const val NOTIFICATION_ID = 33

//private const val CHANNEL_ID = "GeofenceChannel"
private const val CHANNEL_ID = "channel_01"
