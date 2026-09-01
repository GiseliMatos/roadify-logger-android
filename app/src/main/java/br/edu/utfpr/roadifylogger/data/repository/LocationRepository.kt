package br.edu.utfpr.roadifylogger.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import br.edu.utfpr.roadifylogger.data.model.GpsSample
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Streams GPS fixes via the platform [LocationManager]. Requires ACCESS_FINE_LOCATION. */
class LocationRepository(private val context: Context) {

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun observe(minTimeMs: Long = 1000L, minDistanceM: Float = 0f): Flow<GpsSample> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val listener = LocationListener { location: Location ->
            trySend(
                GpsSample(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracyMeters = if (location.hasAccuracy()) location.accuracy else 0f,
                    speedMps = if (location.hasSpeed()) location.speed else 0f,
                    timestampMs = System.currentTimeMillis(),
                ),
            )
        }

        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }

        if (provider == null) {
            close()
            return@callbackFlow
        }

        locationManager.requestLocationUpdates(provider, minTimeMs, minDistanceM, listener, Looper.getMainLooper())

        locationManager.getLastKnownLocation(provider)?.let { last ->
            trySend(
                GpsSample(
                    latitude = last.latitude,
                    longitude = last.longitude,
                    accuracyMeters = if (last.hasAccuracy()) last.accuracy else 0f,
                    speedMps = if (last.hasSpeed()) last.speed else 0f,
                    timestampMs = System.currentTimeMillis(),
                ),
            )
        }

        awaitClose { locationManager.removeUpdates(listener) }
    }
}
