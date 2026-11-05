package com.foodbridge.app.data.repository

import com.foodbridge.app.data.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface ChatRepository {
    fun observeMessages(reservationId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(reservationId: String, senderId: String, text: String): Result<Unit>
}

class FirebaseChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ChatRepository {

    private fun messagesRef(reservationId: String) =
        firestore.collection("reservations").document(reservationId).collection("messages")

    override fun observeMessages(reservationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val registration = messagesRef(reservationId)
            .orderBy("sentAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { it.toObject(ChatMessage::class.java) }.orEmpty()
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun sendMessage(reservationId: String, senderId: String, text: String): Result<Unit> =
        runCatching {
            val message = ChatMessage(
                messageId = UUID.randomUUID().toString(),
                reservationId = reservationId,
                senderId = senderId,
                text = text
            )
            runCatching {
                messagesRef(reservationId).document(message.messageId).set(message).await()
            }
            Unit
        }
}
