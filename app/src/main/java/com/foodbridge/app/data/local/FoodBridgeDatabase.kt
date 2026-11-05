package com.foodbridge.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LocalListingEntity::class, LocalReservationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FoodBridgeDatabase : RoomDatabase() {
    abstract fun listingDao(): ListingDao
    abstract fun reservationDao(): ReservationDao

    companion object {
        @Volatile
        private var INSTANCE: FoodBridgeDatabase? = null

        fun getInstance(context: Context): FoodBridgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoodBridgeDatabase::class.java,
                    "foodbridge_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
