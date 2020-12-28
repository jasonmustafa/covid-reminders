package com.jasonmustafa.maskreminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)

            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val notificationBuilder = NotificationCompat.Builder(context!!, "WashHandsChannel")
                .setSmallIcon(R.drawable.mask)
                .setContentTitle("Wash your hands - you're home!")
                .setContentText("Wash with soap for at least 20 seconds. Swipe me away when you've finished.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Wash with soap for at least 20 seconds. Swipe me away when you've finished.")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(context)) {
                notify(WASH_HANDS_NOTIFICATION_ID, notificationBuilder.build())
            }
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val notificationBuilder = NotificationCompat.Builder(context!!, "WearMaskChannel")
                .setSmallIcon(R.drawable.mask)
                .setContentTitle("Wear a mask - you're heading out!")
                .setContentText("Cover your mouth and nose while outside. Swipe me away if you have a mask.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Cover your mouth and nose while outside. Swipe me away if you have a mask.")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(context)) {
                notify(WEAR_MASK_NOTIFICATION_ID, notificationBuilder.build())
            }
        }
    }
}

private const val WASH_HANDS_NOTIFICATION_ID = 1
private const val WEAR_MASK_NOTIFICATION_ID = 2
private const val TAG = "GeofenceReceiver"
