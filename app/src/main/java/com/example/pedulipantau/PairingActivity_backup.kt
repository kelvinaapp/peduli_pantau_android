//package com.example.pedulipantau
//
//import android.content.Intent
//import android.location.LocationManager
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.os.Handler
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//
//class PairingActivity_backup : AppCompatActivity() {
//
//    private val TAG = "BLESCAN"
//
//    private lateinit var newRecyclerView: RecyclerView
//    private lateinit var btnScan : Button
//
//    private val myBluetoothManager = MyBluetoothManager(this)
//    private val dialogBuilder = DialogBuilder(this)
//    private val  handler = Handler()
//    private val SCANPERIOD: Long = 10000
//
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_pairing)
//
//        myBluetoothManager.checkGrantedPermission()
//
//        //check permission granted
//        if(!(myBluetoothManager.isLocationPermissionGranted &&
//                myBluetoothManager.isBluetoothScanPermissionGranted &&
//                myBluetoothManager.isBluetoothConnectPermissionGranted
//                    )) {
//
//            if (myBluetoothManager.bluetoothAdapter.isEnabled == false)
//                myBluetoothManager.promptEnableBluetooth()
//
//            if (!myBluetoothManager.locationAdapter.isProviderEnabled(LocationManager.GPS_PROVIDER))
//                dialogBuilder.buildAlertMessageNoGps()
//
//        } else {
//
//            myBluetoothManager.checkGrantedPermission()
//            myBluetoothManager.checkRequiredPermission()
//            myBluetoothManager.permissionLauncher = registerForActivityResult(
//                ActivityResultContracts.RequestMultiplePermissions()) { permission ->
//                myBluetoothManager.getPermissionLauncher(permission)
//            }
//            myBluetoothManager.requestPermission()
//        }
//
//
//        btnScan = findViewById<Button>(R.id.btn_scan)
//        btnScan.setOnClickListener {
//            if (myBluetoothManager.isScanning) {
//                myBluetoothManager.stopBleScan()
//                btnScan.text = "SCAN"
//            } else {
//                handler.postDelayed({
//                    myBluetoothManager.stopBleScan()
//                    btnScan.text = "SCAN"
//                }, SCANPERIOD)
//
//                myBluetoothManager.startBleScan()
//                btnScan.text = "Stop Scanning"
//            }
//        }
//
//        var btnBack = findViewById<ImageView>(R.id.btn_back)
//        btnBack.setOnClickListener{
//            finish()
//        }
//
//        newRecyclerView = findViewById(R.id.device_list)
//        newRecyclerView.layoutManager = LinearLayoutManager(this)
//        newRecyclerView.adapter = myBluetoothManager.scanResultAdapter
//
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        myBluetoothManager.onActivityResult(requestCode, resultCode, data)
//    }
//
//}
//
