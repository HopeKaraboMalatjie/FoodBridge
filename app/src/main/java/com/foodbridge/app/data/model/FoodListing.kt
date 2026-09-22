package com.foodbridge.app.data.model

/**
 * FR-2 / FR-3 / FR-4: post, browse/filter and reserve a surplus-food listing.
 * Field names match the Part 1 data dictionary (title, category, quantity,
 * pickupWindowStart/End, status, location.lat/lng, photoUrl).
 */
data class FoodListing(
    val listingId: String = "",
    val donorId: String = "",
    val donorName: String = "",
    val title: String = "",
    val category: ListingCategory = ListingCategory.OTHER,
    val quantity: Int = 1,
    val description: String = "",
    val pickupWindowStart: Long = 0L,
    val pickupWindowEnd: Long = 0L,
    val status: ListingStatus = ListingStatus.AVAILABLE,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val photoUrl: String = "",
    val reservedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ListingCategory { PRODUCE, BAKERY, COOKED_MEAL, OTHER }

enum class ListingStatus { AVAILABLE, RESERVED, COLLECTED, EXPIRED }
