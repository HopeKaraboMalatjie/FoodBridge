package com.foodbridge.app.ui.impact

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
import androidx.navigation.fragment.findNavController
import com.foodbridge.app.FoodBridgeApp
import com.foodbridge.app.R
import com.foodbridge.app.databinding.FragmentImpactProfileBinding
import com.foodbridge.app.ui.auth.AuthViewModel
import com.foodbridge.app.util.LocaleHelper
import com.foodbridge.app.util.UiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.launch
import java.util.Locale

class ImpactProfileFragment : Fragment() {

    private var _binding: FragmentImpactProfileBinding? = null
    private val binding get() = _binding!!

    private val impactViewModel: ImpactViewModel by viewModels {
        val app = requireActivity().application as FoodBridgeApp
        ImpactViewModel.Factory(app.impactRepository)
    }

    private val authViewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as FoodBridgeApp
        AuthViewModel.Factory(app.authRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImpactProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as FoodBridgeApp
        val currentUser = app.authRepository.currentUserId() ?: "demo_user"
        impactViewModel.load(currentUser)

        lifecycleScope.launch {
            val googleAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
            val activeUser = app.authRepository.getCurrentUser() ?: app.authRepository.fetchProfile(currentUser).getOrNull()

            val rawName = googleAccount?.displayName?.takeIf { it.isNotBlank() }
                ?: activeUser?.name?.takeIf { it.isNotBlank() && it != "Member" && it != "Verified User" && it != "User" }
                ?: googleAccount?.email?.substringBefore("@")
                ?: activeUser?.email?.substringBefore("@")
                ?: "Community Member"

            val rawEmail = googleAccount?.email?.takeIf { it.isNotBlank() }
                ?: activeUser?.email?.takeIf { it.isNotBlank() }
                ?: "user@foodbridge.app"

            val formattedName = rawName.replace(".", " ")
                .split(" ")
                .joinToString(" ") { word ->
                    word.lowercase(Locale.getDefault()).replaceFirstChar { char -> char.uppercase() }
                }

            binding.tvUserName.text = formattedName
            binding.tvUserEmail.text = rawEmail
            binding.tvUserRole.text = activeUser?.role?.name ?: "RECIPIENT"
        }

        val currentLang = LocaleHelper.getLanguage(requireContext())
        when (currentLang) {
            "zu" -> binding.rbZulu.isChecked = true
            "af" -> binding.rbAfrikaans.isChecked = true
            else -> binding.rbEnglish.isChecked = true
        }

        binding.rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            val langCode = when (checkedId) {
                R.id.rbZulu -> "zu"
                R.id.rbAfrikaans -> "af"
                else -> "en"
            }
            if (langCode != currentLang) {
                LocaleHelper.setLocale(requireContext(), langCode)
                requireActivity().recreate()
            }
        }

        binding.btnSyncOffline.setOnClickListener {
            lifecycleScope.launch {
                val result = app.listingRepository.syncOfflineData()
                result.onSuccess { count ->
                    Toast.makeText(context, "Synced $count pending items", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Sync failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnClearDatabase.setOnClickListener {
            lifecycleScope.launch {
                app.listingRepository.clearAllData()
                Toast.makeText(context, getString(R.string.clear_database_success), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                app.listingRepository.clearAllData()
            }
            authViewModel.logout()
            findNavController().navigate(R.id.action_impact_to_login)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                impactViewModel.impactState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {}
                        is UiState.Success -> {
                            binding.tvMealsValue.text = state.data.mealsSaved.toString()
                            binding.tvCo2Value.text = String.format(Locale.US, "%.1f", state.data.co2Avoided)
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
