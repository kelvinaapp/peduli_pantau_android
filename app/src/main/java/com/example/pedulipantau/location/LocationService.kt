import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.pedulipantau.location.Workable
import com.google.android.gms.location.*


class LocationService(context: Context) {
    private val mFusedLocationClient: FusedLocationProviderClient
    private val locationCallback: LocationCallback
    private val locationRequest: LocationRequest
    val locationSettingsRequest: LocationSettingsRequest
    private var workable: Workable<GPSPoint>? = null
    fun onChange(workable: Workable<GPSPoint>?) {
        this.workable = workable
    }

    fun stop() {
        Log.i(TAG, "stop() Stopping location tracking")
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private val TAG = LocationService::class.java.simpleName
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000
        fun instance(context: Context): LocationService {
            return LocationService(context)
        }
    }

    init {
        locationRequest = LocationRequest()
        locationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        locationSettingsRequest = builder.build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult) // why? this. is. retarded. Android.
                val currentLocation: Location? = locationResult.lastLocation
                val gpsPoint =
                    GPSPoint(currentLocation!!.getLatitude(), currentLocation.getLongitude())
                Log.i(TAG, "Location Callback results: $gpsPoint")
                if (null != workable) workable!!.work(gpsPoint)
            }
        }

        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            mFusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback, Looper.myLooper()
            )
        }

    }
}