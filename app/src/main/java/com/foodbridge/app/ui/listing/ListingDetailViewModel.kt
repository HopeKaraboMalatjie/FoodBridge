package com.foodbridge.app.ui.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.foodbridge.app.data.model.FoodListing
import com.foodbridge.app.data.repository.ListingRepository
import com.foodbridge.app.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListingDetailViewModel(
    private val repository: ListingRepository,
    private val listingId: String
) : ViewModel() {

    val listingState: StateFlow<UiState<FoodListing?>> = repository.observeListing(listingId)
        .map<FoodListing?, UiState<FoodListing?>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Could not load listing")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private val _reserveState = MutableStateFlow<UiState<Unit>?>(null)
    val reserveState: StateFlow<UiState<Unit>?> = _reserveState.asStateFlow()

    private val _deleteState = MutableStateFlow<UiState<Unit>?>(null)
    val deleteState: StateFlow<UiState<Unit>?> = _deleteState.asStateFlow()

    fun reserve(recipientId: String) {
        _reserveState.value = UiState.Loading
        viewModelScope.launch {
            repository.reserveListing(listingId, recipientId)
                .onSuccess { _reserveState.value = UiState.Success(Unit) }
                .onFailure { _reserveState.value = UiState.Error(it.message ?: "Reservation failed") }
        }
    }

    fun deleteListing() {
        _deleteState.value = UiState.Loading
        viewModelScope.launch {
            repository.deleteListing(listingId)
                .onSuccess { _deleteState.value = UiState.Success(Unit) }
                .onFailure { _deleteState.value = UiState.Error(it.message ?: "Deletion failed") }
        }
    }

    class Factory(
        private val repository: ListingRepository,
        private val listingId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ListingDetailViewModel(repository, listingId) as T
        }
    }
}
