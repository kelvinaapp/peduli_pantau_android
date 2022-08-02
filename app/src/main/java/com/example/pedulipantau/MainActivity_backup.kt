//package com.example.pedulipantau
//
//import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED
//import android.content.*
//import android.os.Build
//import android.os.Bundle
//import android.os.IBinder
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.constraintlayout.widget.ConstraintSet
//import androidx.constraintlayout.widget.Group
//import com.example.pedulipantau.helper.Constant
//import com.example.pedulipantau.helper.PrefHelper
//
//
//class MainActivity_copty : AppCompatActivity(){
//
//    private val TAG = "MainActivityLog"
//    private val REQUEST_GET_DEVICE = 0;
//    private val PASIEN = "EiDoLWlIXYBwx1EaqpU4"
//
//    private val bluetoothPermission = BluetoothPermission(this)
//    lateinit var bluetoothStatus : ImageView
//    private var bluetoothService : BluetoothLeService? = null
//    lateinit var prefHelper: PrefHelper
//
//    var deviceAddress : String = ""
//    private var isServiceBound = false
//
//    private val serviceConnection: ServiceConnection = object : ServiceConnection {
//        //on service success start and bind
//        override fun onServiceConnected(
//            componentName: ComponentName,
//            binder: IBinder
//        ) {
//            bluetoothService = (binder as BluetoothLeService.LocalBinder).getService()
//            bluetoothService?.let { bluetooth ->
//                if (!bluetooth.initialize()) {
//                    Log.e(TAG, "serviceConnection : Unable to initialize Bluetooth")
//                    finish()
//                }
//                Log.d(TAG, "onServiceConnected: Service Connected & Connecting Device")
//                bluetooth.connect(deviceAddress)
//            }
//        }
//
//        override fun onServiceDisconnected(componentName: ComponentName) {
//            Log.i(TAG, "onServiceDisconnected: Service Disconnected")
//            stopMyService()
//        }
//    }
//
//    private fun startMyService() {
//        Log.d(TAG, "startMyService: Start my Service")
//        val intent = Intent(this, BluetoothLeService::class.java)
//        if (Build.VERSION.SDK_INT >= 26) {
//            startForegroundService(intent)
//        } else {
//            startService(intent)
//        }
//        bindMyService()
//        isServiceBound = true
//    }
//
//    private fun bindMyService() {
//        Log.d(TAG, "bindMyService: Bind My Service")
//        Intent(this, BluetoothLeService::class.java).also { intent ->
//            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
//        }
//    }
//
//    private fun unbindMyService() {
//        if (isServiceBound) {
//            unbindService(serviceConnection)
//            bluetoothService = null
//            isServiceBound = false
//        }
//    }
//
//    private fun stopMyService() {
//        unbindMyService()
//        stopService(Intent(this, BluetoothLeService::class.java))
//        changeToDefaultLayout()
//    }
//
//    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            when (intent.action) {
//                BluetoothLeService.ACTION_GATT_CONNECTED -> {
//                    Log.i(TAG, "gattUpdateReceiver: State Connected")
//                }
//                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
//                    Log.i(TAG, "gattUpdateReceiver: State Disconnected")
//                    stopMyService()
//                    Toast.makeText(context,"Perangkat Bluetooth Telah Terputus", Toast.LENGTH_SHORT).show()
//                }
//                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
//                    Log.d(TAG, "onReceive: Services Discovered")
//                    var services = bluetoothService?.getSupportedGattServices()
//                    if (services != null) {
//                        for(service in services) {
//                            Log.i(TAG, "Discovered Services: "+service)
//                        }
//                    }
//                }
//                BluetoothLeService.ACTION_DATA_AVAILABLE -> {
//                    Log.i(TAG, "gattUpdateReceiver: Sensor Data "+intent.getStringExtra("sensorData"))
//                    val sensorData = intent.getStringExtra("sensorData")
//                    val arr = sensorData?.split("=", " ");
//                    if (isServiceBound) {
//                        changeLayoutData(arr)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun makeGattUpdateIntentFilter(): IntentFilter? {
//        return IntentFilter().apply {
//            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
//            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
//            addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
//            addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
//        if (bluetoothService?.connectionState == 0) {
//            stopMyService()
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        unregisterReceiver(gattUpdateReceiver)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stopMyService()
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        prefHelper = PrefHelper(this)
//        if(!prefHelper.getBoolean( Constant.PREF_IS_LOGIN)){
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        val tvPersonName = findViewById<TextView>(R.id.main_person_name)
//        tvPersonName.text = "Hi, " + prefHelper.getString( Constant.PREF_PERSON_NAME)
//
////        Log.d(TAG, "onCreate: Bind My Service")
////        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
////        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE)
////
////
//        if (!bluetoothPermission.isBLESupported()) {
//            Toast.makeText(this, "BLE not supported", Toast.LENGTH_LONG).show()
//        } else {
//
//            bluetoothPermission.checkGrantedPermission()
//            bluetoothPermission.checkRequiredPermission()
//            bluetoothPermission.permissionLauncher = registerForActivityResult(
//                ActivityResultContracts.RequestMultiplePermissions()) { permission ->
//                bluetoothPermission.getPermissionLauncher(permission)
//            }
//            bluetoothPermission.requestPermission()
//        }
//
//        bluetoothStatus = findViewById<ImageView>(R.id.bluetooth_status)
//        bluetoothStatus.setOnClickListener {
//            val intent  = Intent(this, PairingActivity::class.java)
//            startActivityForResult(intent, REQUEST_GET_DEVICE)
//        }
//
//        val btnAddLaporan = findViewById<Button>(R.id.button_add_laporan)
//        btnAddLaporan.setOnClickListener {
//            val intent = Intent(this, QuestionActivity::class.java)
//            startActivity(intent)
//        }
//
//        val laporanStatus = findViewById<ImageView>(R.id.laporan_status)
//        laporanStatus.setOnClickListener {
//            val intent = Intent(this, ListLaporanHarianActivity::class.java)
//            startActivity(intent)
//        }
//
//        val profile = findViewById<ImageView>(R.id.profile)
//        profile.setOnClickListener {
//            val intent = Intent(this, ProfileActivity::class.java)
//            intent.putExtra("Id", PASIEN)
//            startActivity(intent)
//        }
//
//        val faskes = findViewById<ImageView>(R.id.menu_faskes)
//        faskes.setOnClickListener {
//            val intent = Intent(this, FaskesActivity::class.java)
//            startActivity(intent)
//        }
//
//        val edukasi = findViewById<ImageView>(R.id.menu_edukasi)
//        edukasi.setOnClickListener {
//            val intent = Intent(this, ListEdukasiActivity::class.java)
//            startActivity(intent)
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_GET_DEVICE && resultCode == RESULT_OK) {
//            deviceAddress = data?.getStringExtra("result").toString()
//            Log.i(TAG, "onActivityResult: Get Device Address "+deviceAddress)
//            changeToConnectedLayout()
//            startMyService()
//        }
//    }
//
//    private fun changeToConnectedLayout() {
//        val group1 = findViewById<Group>(R.id.group_1)
//        group1.visibility = View.GONE
//        val group2 = findViewById<Group>(R.id.group_2)
//        group2.visibility = View.VISIBLE
//        val constraintLayout = findViewById<ConstraintLayout>(R.id.constraint_layout)
//        val constraintSet = ConstraintSet()
//        constraintSet.clone(constraintLayout);
//        constraintSet.connect(R.id.textView3,ConstraintSet.TOP,R.id.bluetooth_status_connected,ConstraintSet.BOTTOM, 24);
//        constraintSet.applyTo(constraintLayout);
//    }
//
//    private fun changeToDefaultLayout() {
//        val group1 = findViewById<Group>(R.id.group_1)
//        group1.visibility = View.VISIBLE
//        val group2 = findViewById<Group>(R.id.group_2)
//        group2.visibility = View.GONE
//        val constraintLayout = findViewById<ConstraintLayout>(R.id.constraint_layout)
//        val constraintSet = ConstraintSet()
//        constraintSet.clone(constraintLayout);
//        constraintSet.connect(R.id.textView3,ConstraintSet.TOP,R.id.bluetooth_status,ConstraintSet.BOTTOM, 24);
//        constraintSet.applyTo(constraintLayout);
//    }
//
//    private fun changeLayoutData(data: List<String>?){
//        val tvHeartBeat = findViewById<TextView>(R.id.heart_beat)
//        val tvOxygen = findViewById<TextView>(R.id.oxygen)
//        val tvTemperature = findViewById<TextView>(R.id.temperature)
//
//        val ir = data?.get(1)
//        var bpm = data?.get(3)
//        var spo2 = data?.get(5)
//        var temp = data?.get(7)
//
//        if( ir?.toInt()!! < 50000  ) {
//             bpm = "0"
//             spo2 = "0"
//             temp = "0"
//        }
//
//        tvHeartBeat.text = bpm
//        tvOxygen.text = spo2 + "%"
//        tvTemperature.text = temp
//
//    }
//
//
//
//}