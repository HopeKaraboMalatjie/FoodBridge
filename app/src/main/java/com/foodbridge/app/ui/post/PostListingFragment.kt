package com.foodbridge.app.ui.post

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.foodbridge.app.FoodBridgeApp
import com.foodbridge.app.R
import com.foodbridge.app.data.model.ListingCategory
import com.foodbridge.app.databinding.FragmentPostListingBinding
import com.foodbridge.app.util.UiState
import kotlinx.coroutines.launch

class PostListingFragment : Fragment() {

    private var _binding: FragmentPostListingBinding? = null
    private val binding get() = _binding!!

    private var selectedPhotoUri: Uri? = null

    private val postViewModel: PostListingViewModel by viewModels {
        val app = requireActivity().application as FoodBridgeApp
        PostListingViewModel.Factory(app.listingRepository, app.storageRepository)
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPhotoUri = it
            binding.ivPhotoPreview.load(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostListingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = arrayOf(
            getString(R.string.cat_prepared),
            getString(R.string.cat_produce),
            getString(R.string.cat_bakery),
            getString(R.string.cat_canned)
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerCategory.adapter = adapter

        binding.btnSelectPhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSubmitListing.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val quantityStr = binding.etQuantity.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            val quantity = quantityStr.toIntOrNull() ?: 0
            val category = when (binding.spinnerCategory.selectedItemPosition) {
                0 -> ListingCategory.COOKED_MEAL
                1 -> ListingCategory.PRODUCE
                2 -> ListingCategory.BAKERY
                else -> ListingCategory.OTHER
            }

            val app = requireActivity().application as FoodBridgeApp
            val currentUser = app.authRepository.currentUserId() ?: "demo_donor"

            val now = System.currentTimeMillis()
            val pickupEnd = now + (24 * 60 * 60 * 1000)

            postViewModel.submit(
                donorId = currentUser,
                donorName = "Verified Donor",
                title = title,
                category = category,
                quantity = quantity,
                description = description,
                pickupWindowStart = now,
                pickupWindowEnd = pickupEnd,
                latitude = -26.2041,
                longitude = 28.0473,
                photoUri = selectedPhotoUri
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                postViewModel.postState.collect { state ->
                    when (state) {
                        is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                        is UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(context, getString(R.string.submit_listing), Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_post_to_home)
                        }
                        is UiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        }
                        null -> binding.progressBar.visibility = View.GONE
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
