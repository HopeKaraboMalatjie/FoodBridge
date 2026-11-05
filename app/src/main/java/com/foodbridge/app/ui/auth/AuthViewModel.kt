package com.foodbridge.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.foodbridge.app.data.model.User
import com.foodbridge.app.data.model.UserRole
import com.foodbridge.app.data.repository.AuthRepository
import com.foodbridge.app.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val authState: StateFlow<UiState<User>> = _authState.asStateFlow()

    init {
        val uid = repository.currentUserId()
        if (uid != null) {
            fetchProfile(uid)
        } else {
            _authState.value = UiState.Error("Not signed in")
        }
    }

    fun register(name: String, email: String, password: String, role: UserRole) {
        _authState.value = UiState.Loading
        viewModelScope.launch {
            repository.register(name, email, password, role)
                .onSuccess { _authState.value = UiState.Success(it) }
                .onFailure { _authState.value = UiState.Error(it.message ?: "Registration failed") }
        }
    }

    fun login(email: String, password: String) {
        _authState.value = UiState.Loading
        viewModelScope.launch {
            repository.login(email, password)
                .onSuccess { _authState.value = UiState.Success(it) }
                .onFailure { _authState.value = UiState.Error(it.message ?: "Login failed") }
        }
    }

    fun loginWithGoogleToken(idToken: String) {
        _authState.value = UiState.Loading
        viewModelScope.launch {
            repository.loginWithGoogle(idToken)
                .onSuccess { _authState.value = UiState.Success(it) }
                .onFailure { _authState.value = UiState.Error(it.message ?: "Google SSO failed") }
        }
    }

    fun loginWithGoogleFallback(email: String, name: String) {
        _authState.value = UiState.Loading
        viewModelScope.launch {
            repository.loginWithGoogleFallback(email, name)
                .onSuccess { _authState.value = UiState.Success(it) }
                .onFailure { _authState.value = UiState.Error(it.message ?: "Google SSO failed") }
        }
    }

    private fun fetchProfile(uid: String) {
        _authState.value = UiState.Loading
        viewModelScope.launch {
            repository.fetchProfile(uid)
                .onSuccess { _authState.value = UiState.Success(it) }
                .onFailure { _authState.value = UiState.Error(it.message ?: "Could not load profile") }
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = UiState.Error("Not signed in")
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repository) as T
        }
    }
}
