package com.foodbridge.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

data class LatLngPoint(val lat: Double, val lng: Double)

val DEFAULT_LOCATION = LatLngPoint(-26.2041, 28.0473)

object LocationHelper {
    suspend fun fetchLastLocation(context: Context): LatLngPoint {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return DEFAULT_LOCATION
        return runCatching {
            val location = client.lastLocation.await() ?: return DEFAULT_LOCATION
            LatLngPoint(location.latitude, location.longitude)
        }.getOrDefault(DEFAULT_LOCATION)
    }
}
