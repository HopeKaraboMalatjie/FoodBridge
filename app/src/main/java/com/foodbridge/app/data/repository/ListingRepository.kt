package com.foodbridge.app.data.repository

import com.foodbridge.app.data.local.ListingDao
import com.foodbridge.app.data.local.LocalListingEntity
import com.foodbridge.app.data.local.LocalReservationEntity
import com.foodbridge.app.data.local.ReservationDao
import com.foodbridge.app.data.model.FoodListing
import com.foodbridge.app.data.model.ListingCategory
import com.foodbridge.app.data.model.ListingStatus
import com.foodbridge.app.data.remote.CreateListingRequest
import com.foodbridge.app.data.remote.FoodBridgeApi
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

interface ListingRepository {
    fun observeListings(category: ListingCategory? = null): Flow<List<FoodListing>>
    fun observeListing(listingId: String): Flow<FoodListing?>
    suspend fun createListing(request: CreateListingRequest): Result<FoodListing>
    suspend fun reserveListing(listingId: String, recipientId: String): Result<Unit>
    suspend fun deleteListing(listingId: String): Result<Unit>
    suspend fun syncOfflineData(): Result<Int>
    suspend fun clearAllData(): Result<Unit>
}

class FoodBridgeListingRepository(
    private val api: FoodBridgeApi,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val listingDao: ListingDao? = null,
    private val reservationDao: ReservationDao? = null
) : ListingRepository {

    private val listingsCollection by lazy { firestore.collection("listings") }
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun observeListings(category: ListingCategory?): Flow<List<FoodListing>> {
        // Start background sync from Firestore if available
        scope.launch {
            runCatching {
                var query = listingsCollection.whereEqualTo("status", ListingStatus.AVAILABLE.name)
                if (category != null) {
                    query = query.whereEqualTo("category", category.name)
                }
                query.addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        val remoteListings = snapshot.documents.mapNotNull { it.toObject(FoodListing::class.java) }
                        if (remoteListings.isNotEmpty()) {
                            scope.launch {
                                listingDao?.insertListings(remoteListings.map { it.toLocalEntity(isPendingSync = false) })
                            }
                        }
                    }
                }
            }
        }

        // Single Source of Truth: Stream from Room Local DB if available
        if (listingDao != null) {
            val localFlow = if (category != null) {
                listingDao.getListingsByCategory(category.name)
            } else {
                listingDao.getAllListings()
            }
            return localFlow.map { entities ->
                entities.map { entity -> entity.toFoodListing() }
            }
        }

        // Fallback flow if DAO is null
        return flow {
            emit(emptyList())
        }
    }

    override fun observeListing(listingId: String): Flow<FoodListing?> {
        if (listingDao != null) {
            return listingDao.getListingById(listingId).map { entity ->
                entity?.toFoodListing()
            }
        }
        return flow { emit(null) }
    }

    override suspend fun createListing(request: CreateListingRequest): Result<FoodListing> = runCatching {
        val listingId = "listing_" + UUID.randomUUID().toString()
        val listing = FoodListing(
            listingId = listingId,
            donorId = request.donorId,
            donorName = request.donorName,
            title = request.title,
            category = runCatching { ListingCategory.valueOf(request.category) }.getOrDefault(ListingCategory.OTHER),
            quantity = request.quantity,
            description = request.description,
            pickupWindowStart = request.pickupWindowStart,
            pickupWindowEnd = request.pickupWindowEnd,
            status = ListingStatus.AVAILABLE,
            latitude = request.lat,
            longitude = request.lng,
            photoUrl = request.photoUrl,
            createdAt = System.currentTimeMillis()
        )

        // 1. Immediately insert into Room DB so it shows on the Home Feed list right away
        listingDao?.insertListing(listing.toLocalEntity(isPendingSync = true))

        // 2. Try pushing to remote API or Firestore in background
        runCatching {
            val dto = api.createListing(request)
            val remoteListing = listing.copy(listingId = dto.listingId, photoUrl = dto.photoUrl)
            listingDao?.deleteListing(listingId)
            listingDao?.insertListing(remoteListing.toLocalEntity(isPendingSync = false))
            remoteListing
        }.getOrDefault(listing)
    }

    override suspend fun reserveListing(listingId: String, recipientId: String): Result<Unit> = runCatching {
        val entity = listingDao?.getListingByIdDirect(listingId)
        if (entity != null) {
            val updated = entity.copy(status = ListingStatus.RESERVED.name, reservedBy = recipientId)
            listingDao.insertListing(updated)
        }
        runCatching { api.reserveListing(listingId) }
        reservationDao?.insertReservation(
            LocalReservationEntity(
                reservationId = UUID.randomUUID().toString(),
                listingId = listingId,
                recipientId = recipientId,
                reservedAt = System.currentTimeMillis(),
                isPendingSync = true
            )
        )
        Unit
    }

    override suspend fun deleteListing(listingId: String): Result<Unit> = runCatching {
        listingDao?.deleteListing(listingId)
        runCatching {
            listingsCollection.document(listingId).delete()
        }
        Unit
    }

    override suspend fun clearAllData(): Result<Unit> = runCatching {
        listingDao?.clearAllListings()
        reservationDao?.clearAllReservations()
        Unit
    }

    override suspend fun syncOfflineData(): Result<Int> = runCatching {
        var syncedCount = 0
        listingDao?.getPendingSyncListings()?.forEach { entity ->
            val request = CreateListingRequest(
                donorId = entity.donorId,
                donorName = entity.donorName,
                title = entity.title,
                category = entity.category,
                quantity = entity.quantity,
                description = entity.description,
                pickupWindowStart = entity.pickupWindowStart,
                pickupWindowEnd = entity.pickupWindowEnd,
                lat = entity.latitude,
                lng = entity.longitude,
                photoUrl = entity.photoUrl
            )
            runCatching {
                api.createListing(request)
                listingDao?.insertListing(entity.copy(isPendingSync = false))
                syncedCount++
            }
        }
        reservationDao?.getPendingSyncReservations()?.forEach { res ->
            runCatching {
                api.reserveListing(res.listingId)
                reservationDao?.deleteReservation(res.reservationId)
                syncedCount++
            }
        }
        syncedCount
    }

    private fun FoodListing.toLocalEntity(isPendingSync: Boolean): LocalListingEntity {
        return LocalListingEntity(
            listingId = listingId,
            donorId = donorId,
            donorName = donorName,
            title = title,
            category = category.name,
            quantity = quantity,
            description = description,
            pickupWindowStart = pickupWindowStart,
            pickupWindowEnd = pickupWindowEnd,
            status = status.name,
            latitude = latitude,
            longitude = longitude,
            photoUrl = photoUrl,
            reservedBy = reservedBy,
            createdAt = createdAt,
            isPendingSync = isPendingSync
        )
    }

    private fun LocalListingEntity.toFoodListing(): FoodListing {
        return FoodListing(
            listingId = listingId,
            donorId = donorId,
            donorName = donorName,
            title = title,
            category = runCatching { ListingCategory.valueOf(category) }.getOrDefault(ListingCategory.OTHER),
            quantity = quantity,
            description = description,
            pickupWindowStart = pickupWindowStart,
            pickupWindowEnd = pickupWindowEnd,
            status = runCatching { ListingStatus.valueOf(status) }.getOrDefault(ListingStatus.AVAILABLE),
            latitude = latitude,
            longitude = longitude,
            photoUrl = photoUrl,
            reservedBy = reservedBy,
            createdAt = createdAt
        )
    }
}
