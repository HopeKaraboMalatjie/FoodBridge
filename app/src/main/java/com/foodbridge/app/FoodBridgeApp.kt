package com.foodbridge.app

import android.app.Application
import com.foodbridge.app.data.remote.NetworkModule
import com.foodbridge.app.data.repository.FirebaseAuthRepository
import com.foodbridge.app.data.repository.FirebaseChatRepository
import com.foodbridge.app.data.repository.FirebaseStorageRepository
import com.foodbridge.app.data.repository.FoodBridgeImpactRepository
import com.foodbridge.app.data.repository.FoodBridgeListingRepository

/**
 * Minimal manual service-locator so the project has no DI-framework learning curve
 * for the team. Each repository is a lazily-created singleton built on top of the
 * Firebase SDKs and the Retrofit [com.foodbridge.app.data.remote.FoodBridgeApi].
 */
class FoodBridgeApp : Application() {

    val authRepository by lazy { FirebaseAuthRepository() }
    val listingRepository by lazy { FoodBridgeListingRepository(api = NetworkModule.api) }
    val storageRepository by lazy { FirebaseStorageRepository() }
    val chatRepository by lazy { FirebaseChatRepository() }
    val impactRepository by lazy { FoodBridgeImpactRepository(api = NetworkModule.api) }
}
