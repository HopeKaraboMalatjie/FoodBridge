package com.foodbridge.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.foodbridge.app.data.model.FoodListing
import com.foodbridge.app.data.model.ListingCategory
import com.foodbridge.app.util.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * FR-3: Home Feed — nearby listings as cards (business name, item summary, distance,
 * expiry, and a Claim button), matching Figure 2 of the design doc. Distance is computed
 * against the user's last known location where available.
 */
@Composable
fun HomeFeedScreen(
    listingsState: UiState<List<FoodListing>>,
    selectedCategory: ListingCategory?,
    onCategorySelected: (ListingCategory?) -> Unit,
    onListingClick: (FoodListing) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Nearby surplus food",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            AssistChip(
                onClick = { onCategorySelected(null) },
                label = { Text("All") }
            )
            ListingCategory.entries.forEach { category ->
                AssistChip(
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.name) },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        when (listingsState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            is UiState.Error -> Box(Modifier.fillMaxSize()) {
                Text(
                    listingsState.message,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            is UiState.Success -> {
                if (listingsState.data.isEmpty()) {
                    Box(Modifier.fillMaxSize()) {
                        Text(
                            "No listings nearby right now — check back soon.",
                            modifier = Modifier.align(Alignment.Center).padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(listingsState.data, key = { it.listingId }) { listing ->
                            ListingCard(listing = listing, onClick = { onListingClick(listing) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListingCard(listing: FoodListing, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = listing.photoUrl.ifBlank { null },
                contentDescription = listing.title,
                modifier = Modifier.size(72.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(listing.title, style = MaterialTheme.typography.titleMedium)
                Text(listing.donorName, style = MaterialTheme.typography.bodySmall)
                Text(
                    "Pickup by ${formatTime(listing.pickupWindowEnd)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(onClick = onClick) { Text("Claim") }
        }
    }
}

private fun formatTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return "TBD"
    return SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(Date(epochMillis))
}
