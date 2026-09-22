package com.foodbridge.app.viewmodel

import com.foodbridge.app.MainDispatcherRule
import com.foodbridge.app.data.model.User
import com.foodbridge.app.data.model.UserRole
import com.foodbridge.app.data.repository.AuthRepository
import com.foodbridge.app.ui.auth.AuthViewModel
import com.foodbridge.app.util.UiState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/** FR-1: registration & login. */
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: AuthRepository = mock()

    @Test
    fun `login success updates state with user`() = runTest {
        val user = User(userId = "u1", name = "Hope", email = "hope@example.com", role = UserRole.RECIPIENT)
        whenever(repository.currentUserId()).thenReturn(null)
        whenever(repository.login("hope@example.com", "pw123456")).thenReturn(Result.success(user))

        val viewModel = AuthViewModel(repository)
        advanceUntilIdle()
        viewModel.login("hope@example.com", "pw123456")
        advanceUntilIdle()

        val state = viewModel.authState.value
        assertThat(state).isInstanceOf(UiState.Success::class.java)
        assertThat((state as UiState.Success).data).isEqualTo(user)
    }

    @Test
    fun `login failure surfaces an error message`() = runTest {
        whenever(repository.currentUserId()).thenReturn(null)
        whenever(repository.login("bad@example.com", "wrong")).thenReturn(
            Result.failure(Exception("Invalid credentials"))
        )

        val viewModel = AuthViewModel(repository)
        advanceUntilIdle()
        viewModel.login("bad@example.com", "wrong")
        advanceUntilIdle()

        val state = viewModel.authState.value
        assertThat(state).isInstanceOf(UiState.Error::class.java)
        assertThat((state as UiState.Error).message).isEqualTo("Invalid credentials")
    }

    @Test
    fun `register success stores the new profile`() = runTest {
        val user = User(userId = "u2", name = "Chantal", email = "chantal@example.com", role = UserRole.DONOR)
        whenever(repository.currentUserId()).thenReturn(null)
        whenever(repository.register("Chantal", "chantal@example.com", "pw123456", UserRole.DONOR))
            .thenReturn(Result.success(user))

        val viewModel = AuthViewModel(repository)
        advanceUntilIdle()
        viewModel.register("Chantal", "chantal@example.com", "pw123456", UserRole.DONOR)
        advanceUntilIdle()

        val state = viewModel.authState.value
        assertThat(state).isInstanceOf(UiState.Success::class.java)
        assertThat((state as UiState.Success).data.name).isEqualTo("Chantal")
    }

    @Test
    fun `existing session loads profile automatically on init`() = runTest {
        val user = User(userId = "u3", name = "Karabo", email = "karabo@example.com")
        whenever(repository.currentUserId()).thenReturn("u3")
        whenever(repository.fetchProfile("u3")).thenReturn(Result.success(user))

        val viewModel = AuthViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.authState.value
        assertThat(state).isInstanceOf(UiState.Success::class.java)
        assertThat((state as UiState.Success).data.userId).isEqualTo("u3")
    }
}
