package com.jasonmustafa.covidreminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    companion object {
        var notificationId = 1
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            val errorMessage = "geofence_error_message"
            // display error
        } else {
            geofencingEvent.triggeringGeofences.forEach {
                val geofence = it.requestId

                val builder = NotificationCompat.Builder(context!!, "channel_01")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Test Title")
                    .setContentText("test content")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                with(NotificationManagerCompat.from(context)) {
                    notify(notificationId, builder.build())
                }
            }
        }
    }
}
