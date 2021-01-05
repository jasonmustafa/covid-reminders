package com.jasonmustafa.maskreminders

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment

class PermissionRationaleFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(
                    "The background location permission is needed to notify you upon leaving " +
                            "or entering your home. In your app settings, under the \"Location\" permission, select " +
                            "\"Allow all the time\" to allow Mask Reminders to know your location. Your location " +
                            "information will not be collected or shared in any way."
            )
                    .setPositiveButton("ALLOW") { _, _ ->
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }
                    .setNegativeButton("DENY") { _, _ ->
                        // user cancels dialog
                    }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
