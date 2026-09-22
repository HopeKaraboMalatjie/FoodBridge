package com.foodbridge.app.data.repository

import com.foodbridge.app.data.model.FoodListing
import com.foodbridge.app.data.model.ListingCategory
import com.foodbridge.app.data.model.ListingStatus
import com.foodbridge.app.data.remote.CreateListingRequest
import com.foodbridge.app.data.remote.FoodBridgeApi
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * FR-2 (post), FR-3 (browse/filter), FR-4 (reserve).
 *
 * Reads are real-time Firestore listeners (so a reservation shows as "reserved" to every
 * browsing user instantly, per FR-4) while writes that must be atomic or rule-checked go
 * through the custom REST API — matching the "client never talks to the database directly
 * for writes" decision in the Part 1 design.
 */
interface ListingRepository {
    fun observeListings(category: ListingCategory? = null): Flow<List<FoodListing>>
    fun observeListing(listingId: String): Flow<FoodListing?>
    suspend fun createListing(request: CreateListingRequest): Result<FoodListing>
    suspend fun reserveListing(listingId: String, recipientId: String): Result<Unit>
}

class FoodBridgeListingRepository(
    private val api: FoodBridgeApi,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ListingRepository {

    private val listingsCollection by lazy { firestore.collection("listings") }

    override fun observeListings(category: ListingCategory?): Flow<List<FoodListing>> = callbackFlow {
        var query = listingsCollection.whereEqualTo("status", ListingStatus.AVAILABLE.name)
        if (category != null) {
            query = query.whereEqualTo("category", category.name)
        }
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val listings = snapshot?.documents?.mapNotNull { it.toObject(FoodListing::class.java) }.orEmpty()
            trySend(listings)
        }
        awaitClose { registration.remove() }
    }

    override fun observeListing(listingId: String): Flow<FoodListing?> = callbackFlow {
        val registration = listingsCollection.document(listingId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(FoodListing::class.java))
        }
        awaitClose { registration.remove() }
    }

    override suspend fun createListing(request: CreateListingRequest): Result<FoodListing> = runCatching {
        val dto = api.createListing(request)
        FoodListing(
            listingId = dto.listingId,
            donorId = dto.donorId,
            donorName = dto.donorName,
            title = dto.title,
            category = runCatching { ListingCategory.valueOf(dto.category) }.getOrDefault(ListingCategory.OTHER),
            quantity = dto.quantity,
            description = dto.description,
            pickupWindowStart = dto.pickupWindowStart,
            pickupWindowEnd = dto.pickupWindowEnd,
            status = runCatching { ListingStatus.valueOf(dto.status) }.getOrDefault(ListingStatus.AVAILABLE),
            latitude = dto.lat,
            longitude = dto.lng,
            photoUrl = dto.photoUrl
        )
    }

    override suspend fun reserveListing(listingId: String, recipientId: String): Result<Unit> = runCatching {
        // recipientId travels via the authenticated request context in the real Cloud Function;
        // kept as a parameter here so the repository/ViewModel layer stays testable.
        api.reserveListing(listingId)
        Unit
    }
}
