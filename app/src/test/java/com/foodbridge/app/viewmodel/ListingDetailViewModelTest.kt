package com.foodbridge.app.viewmodel

import com.foodbridge.app.MainDispatcherRule
import com.foodbridge.app.data.model.FoodListing
import com.foodbridge.app.data.model.ListingStatus
import com.foodbridge.app.data.repository.ListingRepository
import com.foodbridge.app.ui.listing.ListingDetailViewModel
import com.foodbridge.app.util.UiState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/** FR-4: reserve a listing; a double-reserve is rejected server-side. */
class ListingDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: ListingRepository = mock()

    @Test
    fun `reserve success updates reserveState`() = runTest {
        val listing = FoodListing(listingId = "L1", title = "Bread", status = ListingStatus.AVAILABLE)
        whenever(repository.observeListing("L1")).thenReturn(flowOf(listing))
        whenever(repository.reserveListing("L1", "u1")).thenReturn(Result.success(Unit))

        val viewModel = ListingDetailViewModel(repository, "L1")
        viewModel.reserve("u1")
        advanceUntilIdle()

        assertThat(viewModel.reserveState.value).isInstanceOf(UiState.Success::class.java)
    }

    @Test
    fun `reserve failure surfaces the server's rejection message`() = runTest {
        val listing = FoodListing(listingId = "L1", title = "Bread", status = ListingStatus.RESERVED)
        whenever(repository.observeListing("L1")).thenReturn(flowOf(listing))
        whenever(repository.reserveListing(eq("L1"), eq("u2")))
            .thenReturn(Result.failure(Exception("Listing already reserved")))

        val viewModel = ListingDetailViewModel(repository, "L1")
        viewModel.reserve("u2")
        advanceUntilIdle()

        val state = viewModel.reserveState.value
        assertThat(state).isInstanceOf(UiState.Error::class.java)
        assertThat((state as UiState.Error).message).isEqualTo("Listing already reserved")
    }
}
