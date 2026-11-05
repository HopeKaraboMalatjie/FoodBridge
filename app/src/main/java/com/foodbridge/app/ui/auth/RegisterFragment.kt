package com.foodbridge.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.foodbridge.app.FoodBridgeApp
import com.foodbridge.app.R
import com.foodbridge.app.data.model.UserRole
import com.foodbridge.app.databinding.FragmentRegisterBinding
import com.foodbridge.app.util.UiState
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as FoodBridgeApp
        AuthViewModel.Factory(app.authRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val roles = arrayOf(
            getString(R.string.role_donor),
            getString(R.string.role_recipient)
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles)
        binding.spinnerRole.adapter = adapter

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val selectedRole = if (binding.spinnerRole.selectedItemPosition == 0) UserRole.DONOR else UserRole.RECIPIENT

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.register(name, email, password, selectedRole)
        }

        binding.tvLoginLink.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            findNavController().navigate(R.id.action_register_to_home)
                        }
                        is UiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            if (state.message != "Not signed in") {
                                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                            }
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
