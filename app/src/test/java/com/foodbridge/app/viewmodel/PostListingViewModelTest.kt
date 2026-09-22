package com.foodbridge.app.viewmodel

import com.foodbridge.app.MainDispatcherRule
import com.foodbridge.app.data.model.FoodListing
import com.foodbridge.app.data.model.ListingCategory
import com.foodbridge.app.data.remote.CreateListingRequest
import com.foodbridge.app.data.repository.ListingRepository
import com.foodbridge.app.data.repository.StorageRepository
import com.foodbridge.app.ui.post.PostListingViewModel
import com.foodbridge.app.util.UiState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/** FR-2: post a surplus food listing — validation and the happy path. */
class PostListingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val listingRepository: ListingRepository = mock()
    private val storageRepository: StorageRepository = mock()

    @Test
    fun `blank title is rejected before any repository call`() = runTest {
        val viewModel = PostListingViewModel(listingRepository, storageRepository)

        viewModel.submit(
            donorId = "u1", donorName = "Hope", title = "", category = ListingCategory.PRODUCE,
            quantity = 2, description = "", pickupWindowStart = 0L, pickupWindowEnd = 1000L,
            latitude = 0.0, longitude = 0.0, photoUri = null
        )
        advanceUntilIdle()

        assertThat(viewModel.postState.value).isInstanceOf(UiState.Error::class.java)
    }

    @Test
    fun `pickup end before start is rejected`() = runTest {
        val viewModel = PostListingViewModel(listingRepository, storageRepository)

        viewModel.submit(
            donorId = "u1", donorName = "Hope", title = "Bread", category = ListingCategory.BAKERY,
            quantity = 2, description = "", pickupWindowStart = 2000L, pickupWindowEnd = 1000L,
            latitude = 0.0, longitude = 0.0, photoUri = null
        )
        advanceUntilIdle()

        val state = viewModel.postState.value
        assertThat(state).isInstanceOf(UiState.Error::class.java)
        assertThat((state as UiState.Error).message).contains("pickup window")
    }

    @Test
    fun `valid submission with no photo creates a listing`() = runTest {
        val created = FoodListing(listingId = "L9", title = "Bread")
        whenever(listingRepository.createListing(any())).thenReturn(Result.success(created))

        val viewModel = PostListingViewModel(listingRepository, storageRepository)
        viewModel.submit(
            donorId = "u1", donorName = "Hope", title = "Bread", category = ListingCategory.BAKERY,
            quantity = 3, description = "Fresh loaves", pickupWindowStart = 1000L, pickupWindowEnd = 2000L,
            latitude = -26.2, longitude = 28.0, photoUri = null
        )
        advanceUntilIdle()

        val state = viewModel.postState.value
        assertThat(state).isInstanceOf(UiState.Success::class.java)
        assertThat((state as UiState.Success).data.listingId).isEqualTo("L9")
    }
}
