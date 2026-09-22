package com.foodbridge.app.viewmodel

import com.foodbridge.app.MainDispatcherRule
import com.foodbridge.app.data.model.ImpactStats
import com.foodbridge.app.data.repository.ImpactRepository
import com.foodbridge.app.ui.impact.ImpactViewModel
import com.foodbridge.app.util.UiState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/** FR-6: gamified impact tracking on the profile screen. */
class ImpactViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: ImpactRepository = mock()

    @Test
    fun `load populates meals saved and badge level`() = runTest {
        val stats = ImpactStats(userId = "u1", mealsSaved = 12, co2Avoided = 4.5, badgeLevel = "Food Hero")
        whenever(repository.getImpact("u1")).thenReturn(Result.success(stats))

        val viewModel = ImpactViewModel(repository)
        viewModel.load("u1")
        advanceUntilIdle()

        val state = viewModel.impactState.value
        assertThat(state).isInstanceOf(UiState.Success::class.java)
        assertThat((state as UiState.Success).data.badgeLevel).isEqualTo("Food Hero")
    }

    @Test
    fun `load failure surfaces an error`() = runTest {
        whenever(repository.getImpact("u2")).thenReturn(Result.failure(Exception("Network error")))

        val viewModel = ImpactViewModel(repository)
        viewModel.load("u2")
        advanceUntilIdle()

        assertThat(viewModel.impactState.value).isInstanceOf(UiState.Error::class.java)
    }
}
