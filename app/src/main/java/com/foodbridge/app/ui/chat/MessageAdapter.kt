package com.foodbridge.app.ui.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foodbridge.app.R
import com.foodbridge.app.data.model.ChatMessage
import com.foodbridge.app.databinding.ItemChatMessageBinding

class MessageAdapter(
    private val currentUserId: String
) : ListAdapter<ChatMessage, MessageAdapter.MessageViewHolder>(MessageDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding, currentUserId)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(
        private val binding: ItemChatMessageBinding,
        private val currentUserId: String
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvMessageBody.text = message.text

            val isOutgoing = message.senderId == currentUserId
            val params = binding.tvMessageBody.layoutParams as LinearLayout.LayoutParams

            if (isOutgoing) {
                params.gravity = Gravity.END
                binding.tvMessageBody.setBackgroundResource(R.drawable.bg_message_outgoing)
            } else {
                params.gravity = Gravity.START
                binding.tvMessageBody.setBackgroundResource(R.drawable.bg_message_incoming)
            }
            binding.tvMessageBody.layoutParams = params
        }
    }

    object MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}
