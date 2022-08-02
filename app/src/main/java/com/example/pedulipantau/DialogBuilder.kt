package com.example.pedulipantau

import android.app.Activity
import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.pedulipantau.bluetooth.BluetoothPermission

class DialogBuilder(private val activity: Activity) {

    private val myBluetoothManager = BluetoothPermission(activity)

    fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes",
                DialogInterface.OnClickListener { dialog, id -> myBluetoothManager.promptEnableLocation() })
            .setNegativeButton("No",
                DialogInterface.OnClickListener { dialog, id -> Toast.makeText(activity,
                    "Scan perangkat tidak bisa berjalan tanpa GPS", Toast.LENGTH_LONG).show() })
        val alert: AlertDialog = builder.create()
        alert.show()
    }

}