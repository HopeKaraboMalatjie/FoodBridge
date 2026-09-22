package com.foodbridge.app.ui.impact

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foodbridge.app.data.model.ImpactStats
import com.foodbridge.app.util.UiState

/**
 * FR-6: My Impact / Profile — gamified stats (meals saved, CO2e avoided, badge
 * level) that update after each collection, per Figure 6 of the design doc.
 */
@Composable
fun ImpactProfileScreen(impactState: UiState<ImpactStats>) {
    when (impactState) {
        is UiState.Loading -> Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }

        is UiState.Error -> Box(Modifier.fillMaxSize()) {
            Text(impactState.message, modifier = Modifier.align(Alignment.Center).padding(16.dp))
        }

        is UiState.Success -> {
            val stats = impactState.data
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("My Impact", style = MaterialTheme.typography.headlineSmall)
                Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Badge level: ${stats.badgeLevel}", style = MaterialTheme.typography.titleMedium)
                        Text("Meals saved: ${stats.mealsSaved}", modifier = Modifier.padding(top = 8.dp))
                        Text("CO2e avoided: ${"%.1f".format(stats.co2Avoided)} kg", modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}
