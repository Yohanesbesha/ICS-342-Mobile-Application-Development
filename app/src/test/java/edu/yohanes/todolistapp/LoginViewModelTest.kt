package edu.yohanes.todolistapp

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private val sharedPreferences: SharedPref = mockk(relaxed = true)
    private val todoApiService: TodoApiService = mockk()
    private val context: Context = mockk(relaxed = true)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        viewModel = LoginViewModel(sharedPreferences, todoApiService, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login should succeed with correct credentials`() = runTest {
        // Arrange
        val mockUserId = "userId"
        val mockToken = "token"
        coEvery { todoApiService.login(any(), any()) } returns mockk {
            every { userId } returns mockUserId
            every { token } returns mockToken
        }

        var successCalled = false
        var errorMessage: String? = null

        // Act
        viewModel.updateEmail("yohanes.taddese@example.com")
        viewModel.updatePassword("password")

        viewModel.login(
            onSuccess = { successCalled = true },
            onError = { error -> errorMessage = error }
        )
        advanceUntilIdle()

        // Assert
        assertTrue(successCalled)
        assertNull(errorMessage)
        coVerify { sharedPreferences.saveUserinfo(mockUserId) }
        coVerify { sharedPreferences.saveToken(mockToken) }
    }

    @Test
    fun `login should fail when credentials are incorrect`() = runTest {
        // Arrange
        val errorMessage = "Login failed"
        coEvery { todoApiService.login(any(), any()) } throws Exception(errorMessage)

        var successCalled = false
        var errorCalled = false

        // Act
        viewModel.updateEmail("yohanes.taddese@example.com")
        viewModel.updatePassword("password")

        viewModel.login(
            onSuccess = { successCalled = true },
            onError = { errorCalled = true }
        )
        advanceUntilIdle()

        // Assert
        assertFalse(successCalled)
        assertTrue(errorCalled)
        assertEquals(context.getString(R.string.loginFailed), viewModel.errorMessage.value)
        assertTrue(viewModel.showError.value == true)
    }

    @Test
    fun `login should fail when fields are empty`() = runTest {
        // Arrange
        every { context.getString(R.string.empty_fields_error) } returns "FIELDS CAN NOT BE EMPTY."
        var successCalled = false
        var errorCalled = false

        // Act
        viewModel.login(
            onSuccess = { successCalled = true },
            onError = { errorCalled = true }
        )
        advanceUntilIdle()

        // Assert
        assertFalse(successCalled)
        assertTrue(errorCalled)
        assertEquals("FIELDS CAN NOT BE EMPTY.", viewModel.errorMessage.value)
        assertTrue(viewModel.showError.value == true)
    }

    @Test
    fun `updateEmail should update the email field`() = runTest {
        // Act
        viewModel.updateEmail("yohanes.taddese@example.com")

        // Assert
        assertEquals("yohanes.taddese@example.com", viewModel.email.value)
    }

    @Test
    fun `updatePassword should update the password field`() = runTest {
        // Act
        viewModel.updatePassword("password")

        // Assert
        assertEquals("password", viewModel.password.value)
    }
}
