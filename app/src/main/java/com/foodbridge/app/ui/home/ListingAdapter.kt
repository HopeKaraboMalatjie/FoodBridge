package com.foodbridge.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.foodbridge.app.R
import com.foodbridge.app.data.model.FoodListing
import com.foodbridge.app.databinding.ItemFoodListingBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListingAdapter(
    private val onListingClick: (FoodListing) -> Unit
) : ListAdapter<FoodListing, ListingAdapter.ListingViewHolder>(ListingDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val binding = ItemFoodListingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ListingViewHolder(binding, onListingClick)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ListingViewHolder(
        private val binding: ItemFoodListingBinding,
        private val onListingClick: (FoodListing) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listing: FoodListing) {
            binding.tvListingTitle.text = listing.title
            binding.tvDonorName.text = listing.donorName
            binding.tvCategory.text = listing.category.name
            binding.tvBadgeStatus.text = listing.status.name

            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            val formattedExpiry = dateFormat.format(Date(listing.pickupWindowEnd))
            binding.tvExpiry.text = binding.root.context.getString(R.string.expiry_label, formattedExpiry)

            if (listing.photoUrl.isNotEmpty()) {
                binding.ivListingPhoto.load(listing.photoUrl) {
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_report_image)
                }
            } else {
                binding.ivListingPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            binding.root.setOnClickListener { onListingClick(listing) }
        }
    }

    object ListingDiffCallback : DiffUtil.ItemCallback<FoodListing>() {
        override fun areItemsTheSame(oldItem: FoodListing, newItem: FoodListing): Boolean {
            return oldItem.listingId == newItem.listingId
        }

        override fun areContentsTheSame(oldItem: FoodListing, newItem: FoodListing): Boolean {
            return oldItem == newItem
        }
    }
}
