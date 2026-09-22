package com.foodbridge.app.data.repository

import com.foodbridge.app.data.model.ImpactStats
import com.foodbridge.app.data.remote.FoodBridgeApi

/**
 * FR-6: gamified impact tracking. The badge level and running totals are computed
 * server-side per collection (per the data dictionary), so the client just reads them.
 */
interface ImpactRepository {
    suspend fun getImpact(userId: String): Result<ImpactStats>
}

class FoodBridgeImpactRepository(
    private val api: FoodBridgeApi
) : ImpactRepository {

    override suspend fun getImpact(userId: String): Result<ImpactStats> = runCatching {
        val dto = api.getImpact(userId)
        ImpactStats(
            userId = dto.userId,
            mealsSaved = dto.mealsSaved,
            co2Avoided = dto.co2Avoided,
            badgeLevel = dto.badgeLevel
        )
    }
}
