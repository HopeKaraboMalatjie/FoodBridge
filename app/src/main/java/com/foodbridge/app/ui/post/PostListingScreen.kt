package com.foodbridge.app.ui.post

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.foodbridge.app.data.model.ListingCategory
import com.foodbridge.app.util.UiState

/**
 * FR-2: Post Surplus Food — a form for photo, title, category, quantity, pickup
 * window and auto-location (Figure 4 of the design doc).
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PostListingScreen(
    postState: UiState<*>?,
    latitude: Double,
    longitude: Double,
    onSubmit: (
        title: String,
        category: ListingCategory,
        quantity: Int,
        description: String,
        pickupStart: Long,
        pickupEnd: Long,
        photoUri: Uri?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ListingCategory.OTHER) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Post surplus food", style = MaterialTheme.typography.titleLarge)

        OutlinedButton(onClick = { pickerLauncher.launch("image/*") }, modifier = Modifier.padding(top = 12.dp)) {
            Text(if (photoUri == null) "Add a photo" else "Change photo")
        }
        photoUri?.let {
            AsyncImage(model = it, contentDescription = null, modifier = Modifier.size(120.dp).padding(top = 8.dp))
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title, e.g. 'Surplus vegetable box'") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            OutlinedTextField(
                value = category.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                ListingCategory.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name) },
                        onClick = {
                            category = option
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it.filter { c -> c.isDigit() } },
            label = { Text("Quantity (portions/boxes)") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().height(100.dp).padding(top = 8.dp)
        )

        Text(
            "Pickup window: next 2 hours from now (demo default — wire up a date/time picker for the final build)",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (postState is UiState.Error) {
            Text(postState.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Button(
            onClick = {
                val now = System.currentTimeMillis()
                onSubmit(
                    title,
                    category,
                    quantity.toIntOrNull() ?: 1,
                    description,
                    now,
                    now + 2 * 60 * 60 * 1000,
                    photoUri
                )
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp)
        ) {
            if (postState is UiState.Loading) {
                CircularProgressIndicator(Modifier.size(18.dp))
            } else {
                Text("Post listing")
            }
        }
    }
}
