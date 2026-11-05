package com.foodbridge.app.ui.post

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.foodbridge.app.data.model.FoodListing
import com.foodbridge.app.data.model.ListingCategory
import com.foodbridge.app.data.remote.CreateListingRequest
import com.foodbridge.app.data.repository.ListingRepository
import com.foodbridge.app.data.repository.StorageRepository
import com.foodbridge.app.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostListingViewModel(
    private val listingRepository: ListingRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _postState = MutableStateFlow<UiState<FoodListing>?>(null)
    val postState: StateFlow<UiState<FoodListing>?> = _postState.asStateFlow()

    fun submit(
        donorId: String,
        donorName: String,
        title: String,
        category: ListingCategory,
        quantity: Int,
        description: String,
        pickupWindowStart: Long,
        pickupWindowEnd: Long,
        latitude: Double,
        longitude: Double,
        photoUri: Uri?
    ) {
        if (title.isBlank() || quantity <= 0 || pickupWindowEnd <= pickupWindowStart) {
            _postState.value = UiState.Error("Please fill in a title, quantity and a valid pickup window")
            return
        }

        _postState.value = UiState.Loading
        viewModelScope.launch {
            val photoUrl = photoUri?.let { uri ->
                storageRepository.uploadListingPhoto(uri).getOrElse {
                    _postState.value = UiState.Error(it.message ?: "Photo upload failed")
                    return@launch
                }
            } ?: ""

            val request = CreateListingRequest(
                donorId = donorId,
                donorName = donorName,
                title = title,
                category = category.name,
                quantity = quantity,
                description = description,
                pickupWindowStart = pickupWindowStart,
                pickupWindowEnd = pickupWindowEnd,
                lat = latitude,
                lng = longitude,
                photoUrl = photoUrl
            )

            listingRepository.createListing(request)
                .onSuccess { _postState.value = UiState.Success(it) }
                .onFailure { _postState.value = UiState.Error(it.message ?: "Could not create listing") }
        }
    }

    class Factory(
        private val listingRepository: ListingRepository,
        private val storageRepository: StorageRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PostListingViewModel(listingRepository, storageRepository) as T
        }
    }
}
