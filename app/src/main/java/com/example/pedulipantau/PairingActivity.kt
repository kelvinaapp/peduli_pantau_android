package com.example.pedulipantau

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pedulipantau.bluetooth.BluetoothPermission


@SuppressLint("MissingPermission")
class PairingActivity : AppCompatActivity() {
    private val TAG = "PairingActivityLog"

    private lateinit var newRecyclerView: RecyclerView

    private val SCAN_PERIOD: Long = 5000
    private val SERVICE_UUID = "4faf1004-1fb5-459e-8fcc-c5c9c331914b"

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private var isScanning = false
    private val handler = Handler()

    private val bluetoothPermission = BluetoothPermission(this)
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    val scanResults = arrayListOf<ScanResult>()

    private val scanResultAdapter = ScanResultAdapter(scanResults) { result ->
        // User tapped on a scan result
        if (isScanning) {
            //stop scanning
            bluetoothLeScanner.stopScan(leScanCallback)
        }
        with(result.device) {
            setResult(
                RESULT_OK,
                Intent().putExtra("result", result.device.address)
            )
            Handler().postDelayed({

            }, 2000)
            finish()
        }
    }

    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    Log.i(TAG, "onScanCallBack : Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }
        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "onScanFailed: code $errorCode")
        }
    }

    private fun scanLeDevice() {
        var scanFilters: MutableList<ScanFilter> = ArrayList()
        val scanFilter_1 = ScanFilter.Builder().
        setServiceUuid(ParcelUuid.fromString(SERVICE_UUID)).build()

        if (!isScanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                isScanning = false
                Toast.makeText(this, "Scanning Completed", Toast.LENGTH_SHORT).show()
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanFilters.add(scanFilter_1)
            isScanning = true
            bluetoothLeScanner.startScan(scanFilters, scanSettings, leScanCallback)
            Toast.makeText(this, "Scanning Device", Toast.LENGTH_SHORT).show()
        } else {
            isScanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
            Toast.makeText(this, "Stop Scanning", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pairing)

        bluetoothPermission.checkGrantedPermission()

        //check permission granted
        if(bluetoothPermission.isPermissionGranted()) {
            Log.i(TAG, "onCreate: Permission granted")
            if (bluetoothPermission.bluetoothAdapter.isEnabled == false)
                bluetoothPermission.promptEnableBluetooth()
            if (!bluetoothPermission.locationAdapter.isProviderEnabled(LocationManager.GPS_PROVIDER))
                bluetoothPermission.buildAlertMessageNoGps()

        } else {
            Log.i(TAG, "onCreate: Permission not granted")

            bluetoothPermission.checkGrantedPermission()
            bluetoothPermission.checkRequiredPermission()
            bluetoothPermission.permissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()) { permission ->
                bluetoothPermission.getPermissionLauncher(permission)
            }
            bluetoothPermission.requestPermission()
            finish()
        }

        var btnScan = findViewById<Button>(R.id.btn_scan)
        btnScan.setOnClickListener{
            scanLeDevice()
        }

        var btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener{
            finish()
        }

        newRecyclerView = findViewById(R.id.device_list)
        newRecyclerView.layoutManager = LinearLayoutManager(this)
        newRecyclerView.adapter = scanResultAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        bluetoothPermission.onActivityResult(requestCode, resultCode, data)
    }

}

