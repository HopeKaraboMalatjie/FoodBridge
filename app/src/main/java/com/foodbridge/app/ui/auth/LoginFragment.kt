package com.foodbridge.app.ui.auth

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.foodbridge.app.FoodBridgeApp
import com.foodbridge.app.R
import com.foodbridge.app.databinding.FragmentLoginBinding
import com.foodbridge.app.util.UiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as FoodBridgeApp
        AuthViewModel.Factory(app.authRepository)
    }

    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val token = account?.idToken
                val email = account?.email.orEmpty()
                val name = account?.displayName.orEmpty()

                if (!token.isNullOrBlank()) {
                    authViewModel.loginWithGoogleToken(token)
                } else if (email.isNotEmpty() || name.isNotEmpty()) {
                    authViewModel.loginWithGoogleFallback(email, name)
                } else {
                    val lastAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
                    val lastEmail = lastAccount?.email.orEmpty()
                    val lastName = lastAccount?.displayName.orEmpty()
                    authViewModel.loginWithGoogleFallback(lastEmail, lastName)
                }
            } catch (_: Exception) {
                val lastAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
                val email = lastAccount?.email.orEmpty()
                val name = lastAccount?.displayName.orEmpty()
                authViewModel.loginWithGoogleFallback(email, name)
            }
        } else {
            val lastAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
            val email = lastAccount?.email.orEmpty()
            val name = lastAccount?.displayName.orEmpty()
            authViewModel.loginWithGoogleFallback(email, name)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.login(email, password)
        }

        binding.btnGoogleSso.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        binding.tvRegisterLink.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
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
                            findNavController().navigate(R.id.action_login_to_home)
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
