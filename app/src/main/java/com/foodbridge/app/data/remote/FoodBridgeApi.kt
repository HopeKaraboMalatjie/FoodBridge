package com.foodbridge.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Matches the "Key endpoints" section of the Part 1 API design:
 *  - GET /listings?lat=&lng=&radius= — nearby available listings, paginated
 *  - POST /listings — create a new listing
 *  - POST /listings/{id}/reserve — atomically reserve a listing
 *  - GET /users/{id}/impact — meals-saved / CO2e-avoided / badge level
 *
 * The Android client never writes to Firestore directly for these actions — enforcing
 * "a listing can only be reserved once" server-side rather than trusting the client.
 */
interface FoodBridgeApi {

    @GET("listings")
    suspend fun getListings(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radiusKm: Double = 10.0,
        @Query("page") page: Int = 1
    ): List<ListingDto>

    @retrofit2.http.POST("listings")
    suspend fun createListing(@Body request: CreateListingRequest): ListingDto

    @retrofit2.http.POST("listings/{id}/reserve")
    suspend fun reserveListing(@Path("id") listingId: String): ReserveResponse

    @GET("users/{id}/impact")
    suspend fun getImpact(@Path("id") userId: String): ImpactResponse
}
