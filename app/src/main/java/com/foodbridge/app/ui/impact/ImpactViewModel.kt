package com.foodbridge.app.ui.impact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.foodbridge.app.data.model.ImpactStats
import com.foodbridge.app.data.repository.ImpactRepository
import com.foodbridge.app.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImpactViewModel(private val repository: ImpactRepository) : ViewModel() {

    private val _impactState = MutableStateFlow<UiState<ImpactStats>>(UiState.Loading)
    val impactState: StateFlow<UiState<ImpactStats>> = _impactState.asStateFlow()

    fun load(userId: String) {
        _impactState.value = UiState.Loading
        viewModelScope.launch {
            repository.getImpact(userId)
                .onSuccess { _impactState.value = UiState.Success(it) }
                .onFailure { _impactState.value = UiState.Error(it.message ?: "Could not load impact stats") }
        }
    }

    class Factory(private val repository: ImpactRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ImpactViewModel(repository) as T
        }
    }
}
