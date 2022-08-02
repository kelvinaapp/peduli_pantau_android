package com.example.pedulipantau.bluetooth

import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.*
import android.content.Intent
import android.location.Location
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.pedulipantau.helper.Constant
import com.example.pedulipantau.helper.PrefHelper
import com.example.pedulipantau.notification.NotificationConstant
import com.example.pedulipantau.notification.NotificationHelper
import com.google.android.gms.location.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit


@SuppressLint("MissingPermission")
class BluetoothLeService : Service() {

    var connectionState = STATE_DISCONNECTED
    private val SERVICE_UUID = "4faf1004-1fb5-459e-8fcc-c5c9c331914b"
//    private val CHARACTERISTIC_UUID = "d1cc13fc-560c-4b7d-804b-42364c72a769"
    private val CLIENT_UUID = "00002902-0000-1000-8000-00805f9b34fb"
    private val GATT_MAX_MTU_SIZE = 517

    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private var startMode = START_STICKY    // indicates how to behave if the service is killed
    private var allowRebind = true
    private var isServiceBound = false

    private var builder : NotificationCompat.Builder? = null
    private lateinit var notificationHelper : NotificationHelper

    private val db = Firebase.firestore
    lateinit var prefHelper: PrefHelper
    private var savedDataSensor = arrayListOf<List<String>>()

    private val LOCATION_PERMISSION_REQ_CODE = 1000;
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationCallback: LocationCallback
    lateinit var locationRequest: LocationRequest
    lateinit var locationSettingsRequest: LocationSettingsRequest

    private val bluetoothGattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "onConnectionStateChange: Successfully connected to $deviceAddress")
                    connectionState = STATE_CONNECTED
                    broadcastUpdate(ACTION_GATT_CONNECTED)
                    bluetoothGatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w(TAG,"onConnectionStateChange: Successfully disconnected from $deviceAddress")
                    connectionState = STATE_DISCONNECTED
                    broadcastUpdate(ACTION_GATT_DISCONNECTED)
                    gatt.close()
                }
            } else {
                Log.w(TAG,"onConnectionStateChange: Error $status encountered for $deviceAddress! Disconnecting...")
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                gatt?.requestMtu(GATT_MAX_MTU_SIZE)
                writeCharacteristic(("start_measure").toByteArray())
            } else {
                Log.w(TAG,"onServicesDiscovered: onServicesDiscovered received: $status")
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.i(TAG,"onMtuChanged: ATT MTU changed to $mtu, success: ${status == BluetoothGatt.GATT_SUCCESS}")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i(
                            TAG,"onCharacteristicWrite: Wrote to characteristic $uuid " +
                                "| value: ${String(value,Charsets.UTF_8)}")
                        notifyCharacteristic(characteristic, true)
                    }
                    else -> {
                        Log.e(TAG,"onCharacteristicWrite: Characteristic write failed for $uuid, error: $status")
                    }
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic) {

                val sensorData = String(value, Charsets.UTF_8)
                val arr = sensorData.split("=", " ")
                notificationHelper.notificationManager.notify(ONGOING_NOTIFICATION_ID, builder?.build())

                if(Integer.parseInt(arr[1]) < 50000) {
                    builder?.setContentText("No Finger Detected ???")
                    savedDataSensor.clear()
                }
                else {
                    var message = "BPM: " + arr[3] + ", SpO2: " + arr[5] + "%, Temp: " + arr[7]
                    builder?.setContentText(message)
                    Log.d(TAG, "onCharacteristicChanged: ${message}")
                    addSensorDataToDB(arr)
                    if (savedDataSensor.size < 5) {
                        savedDataSensor.add(arr)
                    } else {
                        checkBodyIsFine()
                        savedDataSensor.clear()
                    }
                }
                
                broadcastUpdate(ACTION_DATA_AVAILABLE, sensorData)
//                Log.d(TAG, "onCharacteristicChanged: SavedDataSensor $savedDataSensor")
//                Log.i("BluetoothGattCallback", "Characteristic ${this.uuid} changed | " +
//                        "value: ${sensorData}")
            }
        }

    }

    private fun addSensorDataToDB(sensorData : List<String>) {
        
        val ir = Integer.parseInt(sensorData[1])
        prefHelper = PrefHelper(applicationContext)
        val userId = prefHelper.getString( Constant.PREF_USER_ID)

        if(userId != null) {
            val data = hashMapOf(
                "id_pasien" to userId,
                "heart_rate" to sensorData[3],
                "spo2" to sensorData[5],
                "body_temperature" to sensorData[7],
                "created_at" to LocalDateTime.now(ZoneId.of("GMT+7")).toString(),
            )

            db.collection("pasien").document(userId).collection("measurement")
                .add(data)
                .addOnSuccessListener {
//                    Log.d(TAG, "addSensorDataToDB: add $data success")
                } .addOnFailureListener { exception ->
                    Log.w(TAG, "addSensorDataToDB: Error -> $exception")
                }
        }
    }

    private fun checkBodyIsFine() {

        var isBpmFine = false
        var isSpo2Fine = false
        var isTempFine = false

        for(data in savedDataSensor) {
            if(isTempFine == false) {
                if(!(data[7].toDouble() < 36.1 || data[7].toDouble() > 38))
                    isTempFine = true
            }

            if(isBpmFine == false) {
                if(!(Integer.parseInt(data[3]) < 40 || Integer.parseInt(data[3]) > 220))
                    isBpmFine = true
            }

            if(isSpo2Fine == false) {
              if (!(Integer.parseInt(data[5]) < 92))
                  isSpo2Fine = true
            }
        }

        showNotification(isBpmFine, isSpo2Fine, isTempFine)
//        Log.d(TAG, "checkBodyIsFine: temp=$isTempFine bpm=$isBpmFine spo2=$isSpo2Fine")
    }

    private fun showNotification(isBpmFine: Boolean, isSpo2Fine: Boolean, isTempFine: Boolean){
//        if(!isTempFine) {
//            notificationHelper.showWarningNotification(
//                "Suhu Tubuh Abnormal",
//                "Suhu tubuh anda berada di luar batas normal"
//            )
//        }

        if(!isBpmFine) {
            notificationHelper.showDangerNotification(
                "Detak Jantung Abnormal",
                "Detak jantung anda berada di luar batas normal"
            )
        }

        if(!isSpo2Fine) {
            notificationHelper.showDangerNotification(
                "Bahaya SpO2 Rendah",
                "Kadar SpO2 anda dibawah 95%"
            )
        }
    }

    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        return bluetoothGatt?.services
    }

    fun initialize(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        getCurrentLocation()
        if (bluetoothAdapter == null) {
            Log.e("Initialize", "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }

    inner class LocalBinder : Binder() {
        fun getService() : BluetoothLeService {
            return this@BluetoothLeService
        }
    }

    private fun broadcastUpdate(action: String, sensorData : String? = null) {
        val intent = Intent(action)
        if(sensorData != null) intent.putExtra("sensorData", sensorData)
        sendBroadcast(intent)
    }


    private fun writeCharacteristic(payload: ByteArray) {
        val sensorServiceUUID = UUID.fromString(SERVICE_UUID)
        val sensorCharUUIDs = bluetoothGatt?.getService(sensorServiceUUID)?.characteristics
        val sensorCharacteristic = sensorCharUUIDs?.get(0)

        val writeType = when {
            sensorCharacteristic?.isWritable() == true -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            sensorCharacteristic?.isWritableWithoutResponse() == true -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> error("Characteristic ${sensorCharacteristic?.uuid} cannot be written to")
        }

        bluetoothGatt?.let { gatt ->
            sensorCharacteristic.writeType = writeType
            sensorCharacteristic.value = payload
            Handler(Looper.getMainLooper()).postDelayed({gatt.writeCharacteristic(sensorCharacteristic)},1000)
        } ?: error("Not connected to a BLE device!")
    }

    private fun notifyCharacteristic(characteristic: BluetoothGattCharacteristic, enabled : Boolean) {
        if(enabled) {
            val cccdUuid = UUID.fromString(CLIENT_UUID)
            val payload = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

            characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
                if (bluetoothGatt?.setCharacteristicNotification( characteristic, enabled) == false)
                {
                    Log.i(TAG, "setCharacteristicNotification disabled for ${characteristic.uuid}")
                    return
                }

                bluetoothGatt?.let { gatt ->
                    cccDescriptor.value = payload
                    Log.i(TAG, "notificationCharacteristic: write descriptor")
                    gatt.writeDescriptor(cccDescriptor)
                } ?: error("Not connected to a BLE device!")

            } ?: Log.e(
                "ConnectionManager",
                "${characteristic.uuid} doesn't contain the CCC descriptor!"
            )
        } else bluetoothGatt?.setCharacteristicNotification( characteristic, enabled)
    }

    private fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    private fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

//    private fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
//        containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

    private fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }


    fun connect(address: String): Boolean {
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
                return true
            } catch (exception: IllegalArgumentException) {
                Log.w("Connect", "Device not found with provided address.")
                return false
            }
        } ?: run {
            Log.w("Connect", "BluetoothAdapter not initialized")
            return false
        }
    }

    private fun close() {
        bluetoothGatt?.let { gatt ->
            gatt.close()
            bluetoothGatt = null
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // The service is starting, due to a call to startService()
        Log.d(TAG, "onStartCommand")

        notificationHelper = NotificationHelper(applicationContext, NotificationConstant.DEFAULT_NOTIFICATION_ID)
        builder = notificationHelper.buildServiceNotification("Peduli Pantau is Active", "Service is Running")
        startForeground(ONGOING_NOTIFICATION_ID, builder?.build())
        return startMode
    }

    override fun onDestroy() {
        // The service is no longer used and is being destroyed
        Log.d(TAG, "onDestroy")
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        close()
        return allowRebind
    }

    override fun onRebind(intent: Intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
        Log.d(TAG, "onRebind")
        Toast.makeText(applicationContext, "onRebind", Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(30)
            maxWaitTime = TimeUnit.SECONDS.toMillis(60)
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        locationSettingsRequest = builder.build()

        locationCallback = object : LocationCallback() {
            
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val currentLocation = locationResult.lastLocation
                Log.d(TAG, "onLocationResult: callback")
                addLocationToDB(currentLocation!!)
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback, Looper.getMainLooper());
    }

    private fun addLocationToDB(location: Location) {

        val coords = hashMapOf(
            "accuracy" to location.accuracy,
            "altitude" to location.altitude,
            "heading" to 0,
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "speed" to location.speed
        )
        val data = hashMapOf(
            "coords" to coords,
            "timestamp" to System.currentTimeMillis(),
        )

        prefHelper = PrefHelper(applicationContext)
        val userId = prefHelper.getString( Constant.PREF_USER_ID)

        if (userId != null) {
            db.collection("pasien").document(userId).collection("location")
                .add(data)
                .addOnSuccessListener {
                    Log.d(TAG, "addLocationToDB: ${data}")
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        }
    }

    companion object {
        const val ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"

        const val TAG: String = "BluetoothLeServiceLog"
        const val ONGOING_NOTIFICATION_ID = 1

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2
    }

}