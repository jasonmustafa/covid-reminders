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
        val washHandsNotificationChannel = NotificationChannel(
            WASH_HANDS_CHANNEL_ID,
            "Wash Hands Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setShowBadge(true)
        }

        washHandsNotificationChannel.enableLights(true)
        washHandsNotificationChannel.lightColor = Color.BLUE
        washHandsNotificationChannel.enableVibration(true)
        washHandsNotificationChannel.description = "Reminders for washing hands and wearing masks."

        val wearMaskNotificationChannel = NotificationChannel(
            WEAR_MASK_CHANNEL_ID,
            "Wear Mask Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setShowBadge(true)
        }

        wearMaskNotificationChannel.enableLights(true)
        wearMaskNotificationChannel.lightColor = Color.BLUE
        wearMaskNotificationChannel.enableVibration(true)
        wearMaskNotificationChannel.description = "Reminders for washing hands and wearing masks."

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannels(
            listOf(
                washHandsNotificationChannel,
                wearMaskNotificationChannel
            )
        )
    }
}

private const val WASH_HANDS_CHANNEL_ID = "WashHandsChannel"
private const val WEAR_MASK_CHANNEL_ID = "WearMaskChannel"
