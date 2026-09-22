package com.foodbridge.app.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foodbridge.app.data.model.ChatMessage
import com.foodbridge.app.util.UiState

/**
 * FR-5: in-app chat scoped to a single reservation (opened after a listing is
 * reserved, per Figure 7's navigation flow), so pickup details can be confirmed
 * without sharing personal phone numbers.
 */
@Composable
fun ChatScreen(
    messagesState: UiState<List<ChatMessage>>,
    currentUserId: String,
    onSend: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (messagesState) {
                is UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Error -> Text(
                    messagesState.message,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                is UiState.Success -> LazyColumn(contentPadding = PaddingValues(12.dp)) {
                    items(messagesState.data, key = { it.messageId }) { message ->
                        val isMine = message.senderId == currentUserId
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                        ) {
                            Card {
                                Text(
                                    message.text,
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text("Message the donor") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                if (draft.isNotBlank()) {
                    onSend(draft)
                    draft = ""
                }
            }) {
                Text("Send")
            }
        }
    }
}
