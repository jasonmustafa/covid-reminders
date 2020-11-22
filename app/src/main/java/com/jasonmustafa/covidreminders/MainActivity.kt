package com.jasonmustafa.covidreminders

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import android.provider.Settings

class MainActivity : AppCompatActivity() {
    private lateinit var geofencingClient: GeofencingClient
    private val runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

//    private val TAG = MainActivity::class.java.simpleName
//    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private val location0 = Location("")
    private lateinit var locations: Array<Location>

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        geofencingClient = LocationServices.getGeofencingClient(this)
        createNotificationChannel(this)

        // onCreate should end here

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

//        val geofencePendingIntent: PendingIntent by lazy {
//            val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
//            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//        }

//        val geofencingClient = LocationServices.getGeofencingClient(this)

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
        checkPermissions()
    }

    private fun checkPermissions() {
        if (foregroundAndBackgroundLocationPermissionGranted()) {
            checkDeviceLocationSettings()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

//    private fun requestPermissions() {
//        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
//                this, Manifest.permission.ACCESS_FINE_LOCATION
//        )
//
//        if (shouldProvideRationale) {
//            Log.i(TAG, "Displaying permission rationale to provide additional context.")
//        } else {
//            Log.i(TAG, "Requesting permission")
//
//            ActivityCompat.requestPermissions(
//                    this@MainActivity,
//                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                    REQUEST_PERMISSIONS_REQUEST_CODE
//            )
//        }
//    }

    /**
     * Determine whether the appropriate location permissions are granted.
     */
    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionGranted(): Boolean {
        val foregroundLocationGranted = (
            PackageManager.PERMISSION_GRANTED
                    == ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION))

        val backgroundLocationGranted =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                true
            }

        return foregroundLocationGranted && backgroundLocationGranted
    }

    /**
     * Request the ACCESS_FINE_LOCATION on all Android versions and ACCESS_BACKGROUND_LOCATION on
     * Android 10+ (Q).
     */
    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionGranted()) {
            return
        }

        var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {
                permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            } else -> {
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            }
        }

        Log.d(TAG, "Request location permissions")
        ActivityCompat.requestPermissions(
                this@MainActivity,
                permissions,
                resultCode
        )
    }

    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority - LocationRequest.PRIORITY_LOW_POWER
        }

        val locationRequestBuilder =
                LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
                settingsClient.checkLocationSettings(locationRequestBuilder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(this@MainActivity,
                    REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                        findViewById(R.id.activivy_main_layout),
                        "Location services must be enabled to use this app.",
                        Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            // TODO: action when location settings are OK
            println("Device location settings OK")
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
                    grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)) {
            Snackbar.make(
                    findViewById(R.id.activivy_main_layout),
                    "You need to allow location permissions all the time to use this app.", Snackbar.LENGTH_INDEFINITE
            )
                    .setAction("Settings") {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
        } else {
            checkDeviceLocationSettings()
        }
    }
}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "MainActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
