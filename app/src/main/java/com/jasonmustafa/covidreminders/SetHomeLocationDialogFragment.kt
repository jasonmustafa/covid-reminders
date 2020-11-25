package com.jasonmustafa.covidreminders

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class SetHomeLocationDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage("test message")
                    .setPositiveButton("test positive"
                    ) { dialog, id ->
                        // FIRE ZE MISSILES!
                    }
                    .setNegativeButton("test negative"
                    ) { dialog, id ->
                        // User cancelled the dialog
                    }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
