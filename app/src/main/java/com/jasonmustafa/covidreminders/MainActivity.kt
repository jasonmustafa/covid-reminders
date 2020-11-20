package com.jasonmustafa.covidreminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    private val location0 = Location("")

    private lateinit var locations: Array<Location>

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        // golden gate bridge
        location0.latitude = 37.8199
        location0.longitude = -122.4783

        println("added location")

        locations = arrayOf(location0)

        val geofences = locations.map {
            Geofence.Builder()
                    .setRequestId("home")
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setCircularRegion(location0.latitude, location0.longitude, 45F)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                            or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
        }

        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofences)
        }.build()

        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val geofencingClient = LocationServices.getGeofencingClient(this)

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnFailureListener {
                println("failure")
            }

            addOnSuccessListener {
                println("success")
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            // TODO
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionState =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
        } else {
            Log.i(TAG, "Requesting permission")

            ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "channel_01"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("channel_01", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }
}
