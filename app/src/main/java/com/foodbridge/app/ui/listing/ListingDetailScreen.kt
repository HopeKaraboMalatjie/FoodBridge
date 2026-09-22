package com.foodbridge.app.ui.listing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.foodbridge.app.data.model.FoodListing
import com.foodbridge.app.data.model.ListingStatus
import com.foodbridge.app.util.UiState

/**
 * FR-4: Listing Detail — photo, quantity, pickup window and description, with
 * Reserve and Message Donor actions (Figure 3 of the design doc). Status updates
 * (e.g. another user reserving it first) arrive live via the Firestore listener.
 */
@Composable
fun ListingDetailScreen(
    listingState: UiState<FoodListing?>,
    reserveState: UiState<Unit>?,
    onReserve: () -> Unit,
    onMessageDonor: () -> Unit
) {
    when (listingState) {
        is UiState.Loading -> Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }

        is UiState.Error -> Box(Modifier.fillMaxSize()) {
            Text(listingState.message, modifier = Modifier.align(Alignment.Center).padding(16.dp))
        }

        is UiState.Success -> {
            val listing = listingState.data
            if (listing == null) {
                Box(Modifier.fillMaxSize()) {
                    Text("This listing is no longer available.", modifier = Modifier.align(Alignment.Center))
                }
                return
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                AsyncImage(
                    model = listing.photoUrl.ifBlank { null },
                    contentDescription = listing.title,
                    modifier = Modifier.fillMaxWidth().height(220.dp)
                )
                Text(listing.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 12.dp))
                Text("From ${listing.donorName}", style = MaterialTheme.typography.bodyMedium)
                Text("Quantity: ${listing.quantity}", style = MaterialTheme.typography.bodyMedium)
                Text("Category: ${listing.category}", style = MaterialTheme.typography.bodyMedium)
                Text("Status: ${listing.status}", style = MaterialTheme.typography.bodyMedium)
                Text(listing.description, modifier = Modifier.padding(top = 8.dp))

                if (reserveState is UiState.Error) {
                    Text(reserveState.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }

                Button(
                    onClick = onReserve,
                    enabled = listing.status == ListingStatus.AVAILABLE && reserveState !is UiState.Loading,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Text(if (listing.status == ListingStatus.AVAILABLE) "Reserve" else "Already claimed")
                }
                OutlinedButton(
                    onClick = onMessageDonor,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Message donor")
                }
            }
        }
    }
}
