package com.jasonmustafa.covidreminders

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import java.util.*

/**
 * Main activity of the application.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var geofencingClient: GeofencingClient
    private val runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private lateinit var locations: Array<Location>
    private lateinit var placesApiKey: String
    private lateinit var placesClient: PlacesClient

    private lateinit var homeLocationTextView: TextView
    private lateinit var homeLatLngTextView: TextView

    private lateinit var sharedPref: SharedPreferences

    /**
     * Called when the activity is starting.
     */
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        geofencingClient = LocationServices.getGeofencingClient(this)
        createNotificationChannel(this)

        homeLocationTextView = findViewById<View>(R.id.homeLocationTextView) as TextView
        homeLatLngTextView = findViewById<View>(R.id.homeLatLngTextView) as TextView

        placesApiKey = Secrets().getPlacesApiKey("com.jasonmustafa.covidreminders")

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, placesApiKey)
        }

        placesClient = Places.createClient(this)

        // set values from sharedpreferences
        sharedPref = applicationContext.getSharedPreferences(
                "com.jasonmustafa.covidreminders.GEO_PREFS", Context.MODE_PRIVATE
        ) ?: return

        val loadedHomeName = sharedPref.getString("HOME_NAME_KEY", "NOT SET")
        val loadedHomeLat: Float = sharedPref.getFloat("HOME_LAT_KEY", 0.0F)
        val loadedHomeLng: Float = sharedPref.getFloat("HOME_LNG_KEY", 0.0F)

        homeLocationTextView.text = loadedHomeName
        homeLatLngTextView.text = "Coordinates: " + loadedHomeLat + ", " + loadedHomeLng

        val autocompleteFragment = supportFragmentManager
                .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?

        autocompleteFragment!!
                .setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                setHomeLocation(place)
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })
    }

    /**
     * Called when activity is resumed after stopped, and is again being displayed to the user;
     * called after onCreate.
     */
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

    /**
     * Determine whether the appropriate location permissions are granted.
     */
    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionGranted(): Boolean {
        val foregroundLocationGranted = (
                PackageManager.PERMISSION_GRANTED
                        == ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ))

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
            }
            else -> {
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
                    exception.startResolutionForResult(
                            this@MainActivity,
                            REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                        findViewById(R.id.activity_main_layout),
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
                    findViewById(R.id.activity_main_layout),
                    "You need to allow location permissions all the time to use this app.",
                    Snackbar.LENGTH_INDEFINITE
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

    /**
     * Set the home location of the user in SharedPreferences after a place is selected from the
     * Google Places autocomplete widget.
     *
     * @param place place the user selected from the autocomplete widget
     *
     * @see SharedPreferences
     * @see Places
     */
    private fun setHomeLocation(place: Place) {
        removeGeofences()

        Log.i(TAG, "Place: " + place.name + ", " + place.id)

        var placeName: String
        var placeId: String
        var placeLat: Double
        var placeLng: Double

        with(sharedPref.edit()) {
            placeName = place.name.toString()
            placeId = place.id.toString()
            placeLat = place.latLng!!.latitude
            placeLng = place.latLng!!.longitude

            putString("HOME_NAME_KEY", placeName)
            putString("HOME_ID_KEY", placeId)
            putFloat("HOME_LAT_KEY", placeLat.toFloat())
            putFloat("HOME_LNG_KEY", placeLng.toFloat())

            apply()
        }

        homeLocationTextView.text = place.name
        homeLatLngTextView.text = "Coordinates: " + placeLat + "," + placeLng

        addGeofence(placeLat, placeLng)
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(lat: Double, lng: Double) {
        val location0 = Location("")

        location0.latitude = lat
        location0.longitude = lng

        println("added location")

        locations = arrayOf(location0)

        val geofences = locations.map {
            Geofence.Builder()
                    .setRequestId("home")
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setCircularRegion(location0.latitude, location0.longitude, 45F)
                    .setTransitionTypes(
                            Geofence.GEOFENCE_TRANSITION_ENTER
                                    or Geofence.GEOFENCE_TRANSITION_EXIT
                    )
                    .build()
        }

        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofences)
        }.build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnFailureListener {
                println("failure")
            }

            addOnSuccessListener {
                println("success")

                with(sharedPref.edit()) {
                    putBoolean("GEOFENCE_IS_ACTIVE_KEY", true)
                    apply()
                }
            }
        }
    }

    /**
     * Remove all created geofences currently stored in the Geofencing client.
     */
    private fun removeGeofences() {
        if (!foregroundAndBackgroundLocationPermissionGranted()) {
            return
        }

        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.d(TAG, "Geofences removed")
                Toast.makeText(applicationContext, "Geofences removed", Toast.LENGTH_SHORT)
                        .show()
            }
            addOnFailureListener {
                // Failed to remove geofences
                Log.d(TAG, "Failed to remove geofences")
            }
        }
    }
}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "MainActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
