package com.foodbridge.app.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTOs for the custom REST layer (Node.js on Firebase Cloud Functions) described in the
 * Part 1 API design: GET /listings, POST /listings, POST /listings/{id}/reserve,
 * GET /users/{id}/impact. Kept separate from the Firestore-facing [com.foodbridge.app.data.model]
 * classes so the wire format can evolve independently of the app's domain models.
 */

@JsonClass(generateAdapter = true)
data class ListingDto(
    @Json(name = "listingId") val listingId: String,
    @Json(name = "donorId") val donorId: String,
    @Json(name = "donorName") val donorName: String,
    @Json(name = "title") val title: String,
    @Json(name = "category") val category: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "description") val description: String,
    @Json(name = "pickupWindowStart") val pickupWindowStart: Long,
    @Json(name = "pickupWindowEnd") val pickupWindowEnd: Long,
    @Json(name = "status") val status: String,
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double,
    @Json(name = "photoUrl") val photoUrl: String
)

@JsonClass(generateAdapter = true)
data class CreateListingRequest(
    @Json(name = "donorId") val donorId: String,
    @Json(name = "donorName") val donorName: String,
    @Json(name = "title") val title: String,
    @Json(name = "category") val category: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "description") val description: String,
    @Json(name = "pickupWindowStart") val pickupWindowStart: Long,
    @Json(name = "pickupWindowEnd") val pickupWindowEnd: Long,
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double,
    @Json(name = "photoUrl") val photoUrl: String
)

@JsonClass(generateAdapter = true)
data class ReserveResponse(
    @Json(name = "reservationId") val reservationId: String,
    @Json(name = "listingId") val listingId: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class ImpactResponse(
    @Json(name = "userId") val userId: String,
    @Json(name = "mealsSaved") val mealsSaved: Int,
    @Json(name = "co2Avoided") val co2Avoided: Double,
    @Json(name = "badgeLevel") val badgeLevel: String
)
