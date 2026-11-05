package com.foodbridge.app.ui.listing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.foodbridge.app.FoodBridgeApp
import com.foodbridge.app.R
import com.foodbridge.app.databinding.FragmentListingDetailBinding
import com.foodbridge.app.util.UiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListingDetailFragment : Fragment() {

    private var _binding: FragmentListingDetailBinding? = null
    private val binding get() = _binding!!

    private var listingId: String = ""

    private val detailViewModel: ListingDetailViewModel by viewModels {
        val app = requireActivity().application as FoodBridgeApp
        ListingDetailViewModel.Factory(app.listingRepository, listingId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listingId = arguments?.getString("listingId").orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListingDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnReserve.setOnClickListener {
            val app = requireActivity().application as FoodBridgeApp
            val currentUser = app.authRepository.currentUserId() ?: "demo_recipient"
            detailViewModel.reserve(currentUser)
        }

        binding.btnMessageDonor.setOnClickListener {
            val bundle = bundleOf("reservationId" to listingId)
            findNavController().navigate(R.id.action_detail_to_chat, bundle)
        }

        binding.btnDeleteListing.setOnClickListener {
            detailViewModel.deleteListing()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                detailViewModel.listingState.collect { state ->
                    when (state) {
                        is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                        is UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            state.data?.let { listing ->
                                binding.tvDetailTitle.text = listing.title
                                binding.tvDetailDonor.text = getString(R.string.donor_label, listing.donorName)

                                val dateFormat = SimpleDateFormat("HH:mm, MMM dd", Locale.getDefault())
                                val startStr = dateFormat.format(Date(listing.pickupWindowStart))
                                val endStr = dateFormat.format(Date(listing.pickupWindowEnd))
                                binding.tvDetailPickupWindow.text = getString(R.string.pickup_window_label, startStr, endStr)
                                binding.tvDetailDescription.text = listing.description

                                if (listing.photoUrl.isNotEmpty()) {
                                    binding.ivDetailPhoto.load(listing.photoUrl)
                                }
                            }
                        }
                        is UiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                detailViewModel.reserveState.collect { state ->
                    when (state) {
                        is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                        is UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(context, getString(R.string.reservation_success), Toast.LENGTH_SHORT).show()
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                detailViewModel.deleteState.collect { state ->
                    when (state) {
                        is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                        is UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(context, getString(R.string.delete_success), Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
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
