package com.foodbridge.app.repository

import com.foodbridge.app.data.model.ListingCategory
import com.foodbridge.app.data.model.ListingStatus
import com.foodbridge.app.data.remote.CreateListingRequest
import com.foodbridge.app.data.remote.FoodBridgeApi
import com.foodbridge.app.data.remote.ImpactResponse
import com.foodbridge.app.data.remote.ListingDto
import com.foodbridge.app.data.remote.ReserveResponse
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Exercises the DTO -> domain-model mapping in [FoodBridgeListingRepository.createListing]
 * without touching Firestore, by faking only the Retrofit [FoodBridgeApi] boundary.
 */
class ListingRepositoryMappingTest {

    @Test
    fun `createListing maps an unknown category string to OTHER instead of crashing`() = runTest {
        val api: FoodBridgeApi = mock()
        val dto = ListingDto(
            listingId = "L1", donorId = "d1", donorName = "Hope's Kitchen", title = "Mystery box",
            category = "NOT_A_REAL_CATEGORY", quantity = 4, description = "", pickupWindowStart = 0L,
            pickupWindowEnd = 0L, status = "AVAILABLE", lat = -26.2, lng = 28.0, photoUrl = ""
        )
        whenever(api.createListing(any())).thenReturn(dto)

        // Repository under test uses only the Retrofit api, so Firestore access in
        // observeListings()/observeListing() is never reached by this test.
        val repository = com.foodbridge.app.data.repository.FoodBridgeListingRepository(
            api = api,
            firestore = mock()
        )

        val result = repository.createListing(
            CreateListingRequest(
                donorId = "d1", donorName = "Hope's Kitchen", title = "Mystery box",
                category = "NOT_A_REAL_CATEGORY", quantity = 4, description = "",
                pickupWindowStart = 0L, pickupWindowEnd = 0L, lat = -26.2, lng = 28.0, photoUrl = ""
            )
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.category).isEqualTo(ListingCategory.OTHER)
        assertThat(result.getOrNull()?.status).isEqualTo(ListingStatus.AVAILABLE)
    }
}
