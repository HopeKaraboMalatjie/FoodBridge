package com.foodbridge.app.util

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

data class LatLngPoint(val lat: Double, val lng: Double)

// Fallback used until the user grants location permission or a fix is available —
// central Johannesburg, roughly where the design doc's pilot users are based.
private val DEFAULT_LOCATION = LatLngPoint(-26.2041, 28.0473)

/**
 * Requests ACCESS_FINE_LOCATION at runtime and returns the device's last known
 * location once available, falling back to [DEFAULT_LOCATION] otherwise. Backs the
 * Home Feed's distance sort (FR-3) and the Map View SDK screen.
 */
@Composable
fun rememberUserLocation(): LatLngPoint {
    val context = LocalContext.current
    var location by remember { mutableStateOf(DEFAULT_LOCATION) }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                location = fetchLastLocation(context) ?: DEFAULT_LOCATION
            }
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            location = fetchLastLocation(context) ?: DEFAULT_LOCATION
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    return location
}

private suspend fun fetchLastLocation(context: android.content.Context): LatLngPoint? = runCatching {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val hasPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!hasPermission) return null
    val location = client.lastLocation.await() ?: return null
    LatLngPoint(location.latitude, location.longitude)
}.getOrNull()
