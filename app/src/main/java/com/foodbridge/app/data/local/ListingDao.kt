package com.foodbridge.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Query("SELECT * FROM local_listings ORDER BY createdAt DESC")
    fun getAllListings(): Flow<List<LocalListingEntity>>

    @Query("SELECT * FROM local_listings WHERE category = :category ORDER BY createdAt DESC")
    fun getListingsByCategory(category: String): Flow<List<LocalListingEntity>>

    @Query("SELECT * FROM local_listings WHERE listingId = :listingId")
    fun getListingById(listingId: String): Flow<LocalListingEntity?>

    @Query("SELECT * FROM local_listings WHERE listingId = :listingId")
    suspend fun getListingByIdDirect(listingId: String): LocalListingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<LocalListingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: LocalListingEntity)

    @Query("SELECT * FROM local_listings WHERE isPendingSync = 1")
    suspend fun getPendingSyncListings(): List<LocalListingEntity>

    @Query("DELETE FROM local_listings WHERE listingId = :listingId")
    suspend fun deleteListing(listingId: String)

    @Query("DELETE FROM local_listings")
    suspend fun clearAllListings()
}
