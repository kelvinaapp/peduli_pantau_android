import java.util.*

/**
 * * Created by alejandro.tkachuk
 */
class GPSPoint {
    var latitude: Double
    var longitude: Double
    var date: Date? = null
    var lastUpdate: Long = 0

    constructor(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        date = Date()
        lastUpdate = System.currentTimeMillis()
    }

    constructor(latitude: Double?, longitude: Double?) {
        this.latitude = latitude!!
        this.longitude = longitude!!
    }

    override fun toString(): String {
        return "(" + latitude + ", " + longitude + ")"
    }
}