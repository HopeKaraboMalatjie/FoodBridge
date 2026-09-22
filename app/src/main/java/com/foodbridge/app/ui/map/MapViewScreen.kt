package com.foodbridge.app.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foodbridge.app.data.model.FoodListing
import com.foodbridge.app.util.UiState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * FR-3 / SDK integration requirement: Map View — nearby listings plotted around the
 * user's current location (Figure 5 of the design doc), using the Google Maps
 * Compose SDK.
 */
@Composable
fun MapViewScreen(
    listingsState: UiState<List<FoodListing>>,
    userLat: Double,
    userLng: Double,
    onMarkerClick: (FoodListing) -> Unit
) {
    when (listingsState) {
        is UiState.Loading -> Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }

        is UiState.Error -> Box(Modifier.fillMaxSize()) {
            Text(listingsState.message, modifier = Modifier.align(Alignment.Center).padding(16.dp))
        }

        is UiState.Success -> {
            val cameraPositionState: CameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(LatLng(userLat, userLng), 13f)
            }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = LatLng(userLat, userLng)),
                    title = "You are here"
                )
                listingsState.data.forEach { listing ->
                    Marker(
                        state = MarkerState(position = LatLng(listing.latitude, listing.longitude)),
                        title = listing.title,
                        snippet = listing.donorName,
                        onClick = {
                            onMarkerClick(listing)
                            true
                        }
                    )
                }
            }
        }
    }
}
