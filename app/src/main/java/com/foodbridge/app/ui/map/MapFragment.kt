package com.foodbridge.app.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.foodbridge.app.FoodBridgeApp
import com.foodbridge.app.R
import com.foodbridge.app.data.model.FoodListing
import com.foodbridge.app.databinding.FragmentMapBinding
import com.foodbridge.app.ui.home.HomeViewModel
import com.foodbridge.app.util.UiState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private var listings: List<FoodListing> = emptyList()

    private val homeViewModel: HomeViewModel by viewModels {
        val app = requireActivity().application as FoodBridgeApp
        HomeViewModel.Factory(app.listingRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.listingsState.collect { state ->
                    if (state is UiState.Success) {
                        listings = state.data
                        updateMarkers()
                    }
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.setOnInfoWindowClickListener { marker ->
            val listing = marker.tag as? FoodListing
            listing?.let {
                val bundle = bundleOf("listingId" to it.listingId)
                findNavController().navigate(R.id.action_map_to_detail, bundle)
            }
        }
        updateMarkers()
    }

    private fun updateMarkers() {
        val map = googleMap ?: return
        map.clear()

        val defaultPos = LatLng(-26.2041, 28.0473)
        if (listings.isNotEmpty()) {
            listings.forEach { listing ->
                val pos = LatLng(listing.latitude, listing.longitude)
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title(listing.title)
                        .snippet("Donor: ${listing.donorName}")
                )
                marker?.tag = listing
            }
            val first = listings.first()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(first.latitude, first.longitude), 12f))
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPos, 10f))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }
}
