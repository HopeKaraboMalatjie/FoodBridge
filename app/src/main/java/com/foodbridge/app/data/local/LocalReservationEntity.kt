package com.foodbridge.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_reservations")
data class LocalReservationEntity(
    @PrimaryKey val reservationId: String,
    val listingId: String,
    val recipientId: String,
    val reservedAt: Long,
    val isPendingSync: Boolean = false
)
