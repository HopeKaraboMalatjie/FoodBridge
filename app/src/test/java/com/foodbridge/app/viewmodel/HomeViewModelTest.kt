package com.foodbridge.app.viewmodel

import app.cash.turbine.test
import com.foodbridge.app.MainDispatcherRule
import com.foodbridge.app.data.model.FoodListing
import com.foodbridge.app.data.model.ListingCategory
import com.foodbridge.app.data.repository.ListingRepository
import com.foodbridge.app.ui.home.HomeViewModel
import com.foodbridge.app.util.UiState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/** FR-3: browse and filter listings. */
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: ListingRepository = mock()

    @Test
    fun `default state shows all listings`() = runTest {
        val listings = listOf(
            FoodListing(listingId = "1", title = "Bread"),
            FoodListing(listingId = "2", title = "Veg box")
        )
        whenever(repository.observeListings(isNull())).thenReturn(flowOf(listings))

        val viewModel = HomeViewModel(repository)

        viewModel.listingsState.test {
            // Loading is the stateIn initial value; skip it if it appears before the real emission.
            var state = awaitItem()
            if (state is UiState.Loading) state = awaitItem()
            assertThat(state).isInstanceOf(UiState.Success::class.java)
            assertThat((state as UiState.Success).data).hasSize(2)
        }
    }

    @Test
    fun `changing category re-queries the repository`() = runTest {
        whenever(repository.observeListings(isNull())).thenReturn(flowOf(emptyList()))
        whenever(repository.observeListings(eq(ListingCategory.BAKERY)))
            .thenReturn(flowOf(listOf(FoodListing(listingId = "3", title = "Bagels", category = ListingCategory.BAKERY))))

        val viewModel = HomeViewModel(repository)
        viewModel.setCategory(ListingCategory.BAKERY)

        viewModel.listingsState.test {
            var state = awaitItem()
            while (state is UiState.Loading || (state is UiState.Success && state.data.isEmpty())) {
                state = awaitItem()
            }
            assertThat(state).isInstanceOf(UiState.Success::class.java)
            assertThat((state as UiState.Success).data.first().category).isEqualTo(ListingCategory.BAKERY)
        }
    }
}
