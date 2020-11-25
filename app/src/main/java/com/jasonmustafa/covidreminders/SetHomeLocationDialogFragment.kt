//package com.jasonmustafa.covidreminders
//
//import android.app.AlertDialog
//import android.app.Dialog
//import android.content.Context
//import android.os.Bundle
//import android.util.Log
//import androidx.fragment.app.DialogFragment
//import com.google.android.gms.common.api.Status
//import com.google.android.libraries.places.api.Places
//import com.google.android.libraries.places.api.model.Place
//import com.google.android.libraries.places.api.net.PlacesClient
//import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
//
//
//class SetHomeLocationDialogFragment : DialogFragment() {
////    private lateinit var apiKey: String
////    private lateinit var placesClient: PlacesClient
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
////        apiKey = getString(R.string.api_key)
////
////        if (!Places.isInitialized()) {
////            Places.initialize(MyApp.getContext(), apiKey)
////        }
////
////        val autocompleteFragment =
////            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
////
////        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME))
////
////        autocompleteFragment!!.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))
////
////        autocompleteFragment.setOnPlaceSelectedListener(object :
////            PlaceSelectionListener {
////            override fun onPlaceSelected(place: Place) {
////                // TODO: Get info about the selected place.
////                Log.i(TAG, "Place: " + place.name + ", " + place.id)
////            }
////
////            override fun onError(status: Status) {
////                // TODO: Handle the error.
////                Log.i(TAG, "An error occurred: $status")
////            }
////        })
//
//        return activity?.let {
//            // Use the Builder class for convenient dialog construction
//            val builder = AlertDialog.Builder(it)
//            val inflater = requireActivity().layoutInflater;
//
//            builder.setView(inflater.inflate(R.layout.layout_set_home_location, null))
//                    .setPositiveButton(
//                        "set"
//                    ) { dialog, id ->
//                        // set home location
//                    }
//                    .setNegativeButton(
//                        "cancel"
//                    ) { dialog, id ->
//                        // User cancelled the dialog
//                    }
//            // Create the AlertDialog object and return it
//            builder.create()
//        } ?: throw IllegalStateException("Activity cannot be null")
//    }
//}
//
//private const val TAG = "SetHome"
//
///*
//val autocompleteFragment =
//                            fragmentManager?.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
//
//                        autocompleteFragment!!.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))
//
//                        autocompleteFragment.setOnPlaceSelectedListener(object :
//                            PlaceSelectionListener {
//                            override fun onPlaceSelected(place: Place) {
//                                // TODO: Get info about the selected place.
//                                Log.i(TAG, "Place: " + place.name + ", " + place.id)
//                            }
//
//                            override fun onError(status: Status) {
//                                // TODO: Handle the error.
//                                Log.i(TAG, "An error occurred: $status")
//                            }
//                        })
// */
