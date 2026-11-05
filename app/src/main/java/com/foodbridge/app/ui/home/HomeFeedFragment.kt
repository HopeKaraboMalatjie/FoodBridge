package com.foodbridge.app.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodbridge.app.FoodBridgeApp
import com.foodbridge.app.R
import com.foodbridge.app.data.model.FoodListing
import com.foodbridge.app.data.model.ListingCategory
import com.foodbridge.app.databinding.FragmentHomeFeedBinding
import com.foodbridge.app.util.UiState
import kotlinx.coroutines.launch

class HomeFeedFragment : Fragment() {

    private var _binding: FragmentHomeFeedBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels {
        val app = requireActivity().application as FoodBridgeApp
        HomeViewModel.Factory(app.listingRepository)
    }

    private lateinit var listingAdapter: ListingAdapter
    private var allListings: List<FoodListing> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listingAdapter = ListingAdapter { listing ->
            val bundle = bundleOf("listingId" to listing.listingId)
            findNavController().navigate(R.id.action_home_to_detail, bundle)
        }

        binding.rvListings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listingAdapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }

        binding.chipGroupCategory.setOnCheckedChangeListener { _, checkedId ->
            val category = when (checkedId) {
                R.id.chipPrepared -> ListingCategory.COOKED_MEAL
                R.id.chipProduce -> ListingCategory.PRODUCE
                R.id.chipBakery -> ListingCategory.BAKERY
                R.id.chipCanned -> ListingCategory.OTHER
                else -> null
            }
            homeViewModel.setCategory(category)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.listingsState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.swipeRefresh.isRefreshing = true
                        }
                        is UiState.Success -> {
                            binding.swipeRefresh.isRefreshing = false
                            allListings = state.data
                            filterSearch(binding.etSearch.text.toString())
                        }
                        is UiState.Error -> {
                            binding.swipeRefresh.isRefreshing = false
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun filterSearch(query: String) {
        val filtered = if (query.isBlank()) {
            allListings
        } else {
            allListings.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.donorName.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }
        listingAdapter.submitList(filtered)
        binding.tvEmptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
