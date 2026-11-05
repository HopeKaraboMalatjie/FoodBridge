package com.foodbridge.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReservationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: LocalReservationEntity)

    @Query("SELECT * FROM local_reservations WHERE isPendingSync = 1")
    suspend fun getPendingSyncReservations(): List<LocalReservationEntity>

    @Query("DELETE FROM local_reservations WHERE reservationId = :reservationId")
    suspend fun deleteReservation(reservationId: String)

    @Query("DELETE FROM local_reservations")
    suspend fun clearAllReservations()
}
