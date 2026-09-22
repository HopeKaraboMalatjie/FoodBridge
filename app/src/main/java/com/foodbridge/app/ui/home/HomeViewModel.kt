package com.foodbridge.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodbridge.app.data.model.FoodListing
import com.foodbridge.app.data.model.ListingCategory
import com.foodbridge.app.data.repository.ListingRepository
import com.foodbridge.app.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** FR-3: browse and filter listings, live-updated from Firestore. */
class HomeViewModel(private val repository: ListingRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<ListingCategory?>(null)
    val selectedCategory: StateFlow<ListingCategory?> = _selectedCategory.asStateFlow()

    val listingsState: StateFlow<UiState<List<FoodListing>>> = _selectedCategory
        .flatMapLatest { category -> repository.observeListings(category) }
        .map<List<FoodListing>, UiState<List<FoodListing>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Could not load listings")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun setCategory(category: ListingCategory?) {
        _selectedCategory.value = category
    }
}
