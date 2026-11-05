package com.foodbridge.app

import android.app.Application
import android.content.Context
import com.foodbridge.app.data.local.FoodBridgeDatabase
import com.foodbridge.app.data.remote.NetworkModule
import com.foodbridge.app.data.repository.FirebaseAuthRepository
import com.foodbridge.app.data.repository.FirebaseChatRepository
import com.foodbridge.app.data.repository.FirebaseStorageRepository
import com.foodbridge.app.data.repository.FoodBridgeImpactRepository
import com.foodbridge.app.data.repository.FoodBridgeListingRepository
import com.foodbridge.app.util.LocaleHelper

class FoodBridgeApp : Application() {

    val database by lazy { FoodBridgeDatabase.getInstance(this) }
    val authRepository by lazy { FirebaseAuthRepository(this) }
    val listingRepository by lazy {
        FoodBridgeListingRepository(
            api = NetworkModule.api,
            listingDao = database.listingDao(),
            reservationDao = database.reservationDao()
        )
    }
    val storageRepository by lazy { FirebaseStorageRepository() }
    val chatRepository by lazy { FirebaseChatRepository() }
    val impactRepository by lazy {
        FoodBridgeImpactRepository(
            api = NetworkModule.api,
            authRepository = authRepository,
            listingDao = database.listingDao()
        )
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }
}
