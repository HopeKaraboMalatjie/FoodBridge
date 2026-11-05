package com.foodbridge.app.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodbridge.app.FoodBridgeApp
import com.foodbridge.app.databinding.FragmentChatBinding
import com.foodbridge.app.util.UiState
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private var reservationId: String = ""
    private var currentUserId: String = ""

    private val chatViewModel: ChatViewModel by viewModels {
        val app = requireActivity().application as FoodBridgeApp
        ChatViewModel.Factory(app.chatRepository, reservationId)
    }

    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reservationId = arguments?.getString("reservationId").orEmpty()
        val app = requireActivity().application as FoodBridgeApp
        currentUserId = app.authRepository.currentUserId() ?: "demo_user"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageAdapter = MessageAdapter(currentUserId)
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messageAdapter
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                chatViewModel.send(currentUserId, text)
                binding.etMessageInput.text.clear()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.messagesState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {}
                        is UiState.Success -> {
                            messageAdapter.submitList(state.data)
                            if (state.data.isNotEmpty()) {
                                binding.rvMessages.scrollToPosition(state.data.size - 1)
                            }
                        }
                        is UiState.Error -> {
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
