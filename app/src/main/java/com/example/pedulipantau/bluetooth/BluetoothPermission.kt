package com.example.pedulipantau.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import java.util.*


@SuppressLint("MissingPermission")
class BluetoothPermission(private var activity: Activity){

    //normal variable
    lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    var isBluetoothScanPermissionGranted = false
    var isBluetoothConnectPermissionGranted = false
    var isLocationPermissionGranted = false
    var requiredPermission : MutableList<String> = ArrayList()

    //const
    private val ENABLE_BLUETOOTH_REQUEST_CODE = 1
    private val ENABLE_LOCATION_REQUEST_CODE = 2

    // object variable
    val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    val locationAdapter: LocationManager by lazy {
        activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }


    /*
     FUNCTION
    */

    /* BLE Permission */
    
    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    fun isBLESupported() : Boolean {
        // Check to see if the BLE feature is available.
        activity.packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(activity, "BLE not supported", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    fun checkGrantedPermission() {
        if(Build.VERSION.SDK_INT >= 31) {
            isBluetoothScanPermissionGranted = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            isBluetoothConnectPermissionGranted = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        }else {
            isBluetoothScanPermissionGranted = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
            isBluetoothConnectPermissionGranted = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
            isLocationPermissionGranted = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        else
            isLocationPermissionGranted = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    }

    fun checkRequiredPermission() {
        //mengisi daftar permission
        if(!isBluetoothConnectPermissionGranted || !isBluetoothScanPermissionGranted) {
            if(Build.VERSION.SDK_INT >= 31) {
                requiredPermission.add(Manifest.permission.BLUETOOTH_SCAN)
                requiredPermission.add(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                requiredPermission.add(Manifest.permission.BLUETOOTH_ADMIN)
                requiredPermission.add(Manifest.permission.BLUETOOTH)
            }
        }

        if(!isLocationPermissionGranted) {
            if (Build.VERSION.SDK_INT > 28)
                requiredPermission.add(Manifest.permission.ACCESS_FINE_LOCATION)
            else
                requiredPermission.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    fun getPermissionLauncher(permission: Map<String, @JvmWildcard Boolean>) {
        if(Build.VERSION.SDK_INT >= 31) {
            isBluetoothScanPermissionGranted = permission[Manifest.permission.BLUETOOTH_SCAN] ?: isBluetoothScanPermissionGranted
            isBluetoothConnectPermissionGranted = permission[Manifest.permission.BLUETOOTH_CONNECT] ?: isBluetoothConnectPermissionGranted
        } else {
            isBluetoothScanPermissionGranted = permission[Manifest.permission.BLUETOOTH_ADMIN] ?: isBluetoothScanPermissionGranted
            isBluetoothConnectPermissionGranted = permission[Manifest.permission.BLUETOOTH] ?: isBluetoothConnectPermissionGranted
        }

        if (Build.VERSION.SDK_INT > 28)
            isLocationPermissionGranted = permission[Manifest.permission.ACCESS_FINE_LOCATION] ?: isLocationPermissionGranted
        else
            isLocationPermissionGranted = permission[Manifest.permission.ACCESS_COARSE_LOCATION] ?: isLocationPermissionGranted

    }

    fun requestPermission() {
        if(requiredPermission.isNotEmpty())
            permissionLauncher.launch(requiredPermission.toTypedArray())
    }
    
    fun promptEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        if(isBluetoothConnectPermissionGranted && isBluetoothScanPermissionGranted) 
            activity.startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        
    }

    fun promptEnableLocation() {
        val enableLocIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        if(isLocationPermissionGranted) 
            activity.startActivityForResult(enableLocIntent, ENABLE_LOCATION_REQUEST_CODE)
            
    }

    fun isPermissionGranted() : Boolean {
        return isLocationPermissionGranted && isBluetoothScanPermissionGranted && isBluetoothConnectPermissionGranted
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                //bisa ditambahkan dialog
                if (resultCode != Activity.RESULT_OK)
                    promptEnableBluetooth()
            }
        }
    }

    fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes",
                DialogInterface.OnClickListener { dialog, id -> promptEnableLocation() })
            .setNegativeButton("No",
                DialogInterface.OnClickListener { dialog, id -> Toast.makeText(activity,
                    "Scan perangkat tidak bisa berjalan tanpa GPS", Toast.LENGTH_LONG).show()
                    buildAlertMessageNoGps()})
        val alert: AlertDialog = builder.create()
        alert.show()
    }

}