package com.foodbridge.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_listings")
data class LocalListingEntity(
    @PrimaryKey val listingId: String,
    val donorId: String,
    val donorName: String,
    val title: String,
    val category: String,
    val quantity: Int,
    val description: String,
    val pickupWindowStart: Long,
    val pickupWindowEnd: Long,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrl: String,
    val reservedBy: String?,
    val createdAt: Long,
    val isPendingSync: Boolean = false
)
