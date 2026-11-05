package com.foodbridge.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.foodbridge.app.data.model.ChatMessage
import com.foodbridge.app.data.repository.ChatRepository
import com.foodbridge.app.util.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val reservationId: String
) : ViewModel() {

    val messagesState: StateFlow<UiState<List<ChatMessage>>> = repository.observeMessages(reservationId)
        .map<List<ChatMessage>, UiState<List<ChatMessage>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Could not load messages")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun send(senderId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(reservationId, senderId, text)
        }
    }

    class Factory(
        private val repository: ChatRepository,
        private val reservationId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(repository, reservationId) as T
        }
    }
}
