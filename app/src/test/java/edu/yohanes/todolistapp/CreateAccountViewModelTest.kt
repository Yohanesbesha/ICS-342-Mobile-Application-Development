package edu.yohanes.todolistapp

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.yohanes.todolistapp.data.User
import edu.yohanes.todolistapp.data.UserResponse
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@ExperimentalCoroutinesApi
class CreateAccountViewModelTest {

    @get:Rule
    var instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private val sharedPreferences: SharedPref = mockk(relaxed = true)
    private val todoApiService: TodoApiService = mockk()
    private val context: Context = mockk(relaxed = true)

    private lateinit var viewModel: CreateAccountViewModel

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = CreateAccountViewModel(sharedPreferences, todoApiService, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateUsername should update username field`() = runTest {
        // Arrange
        val newName = "John Doe"

        // Act
        viewModel.updateUsername(newName)

        // Assert
        assertEquals(newName, viewModel.name.value)
    }

    @Test
    fun `updateEmail should update email field`() = runTest {
        // Arrange
        val newEmail = "john.doe@example.com"

        // Act
        viewModel.updateEmail(newEmail)

        // Assert
        assertEquals(newEmail, viewModel.email.value)
    }

    @Test
    fun `updatePassword should update password field`() = runTest {
        // Arrange
        val newPassword = "password123"

        // Act
        viewModel.updatePassword(newPassword)

        // Assert
        assertEquals(newPassword, viewModel.password.value)
    }

    @Test
    fun `createAccount should fail when fields are empty`() = runTest {
        // Arrange
        coEvery { context.getString(R.string.create_account_empty_fields_error) } returns "Fields cannot be empty"

        // Act
        viewModel.createAccount({}, {})

        // Assert
        assertTrue(viewModel.showError.value == true)
        assertTrue(viewModel.errorMessage.value?.contains("Fields cannot be empty") == true)
    }

    @Test
    fun `createAccount should succeed with valid inputs`() = runTest {
        // Arrange
        val dummyUser = User("Yohanes Taddese", "yohanes.taddese@example.com", "password123")
        val response = UserResponse("God is good", "God1", name = "Yohanes")
        coEvery { todoApiService.register(any(), dummyUser) } returns response
        coEvery { sharedPreferences.saveUserinfo(response.userId) } just Runs
        coEvery { sharedPreferences.saveToken(response.token) } just Runs

        // Act
        viewModel.updateUsername(dummyUser.name)
        viewModel.updateEmail(dummyUser.email)
        viewModel.updatePassword(dummyUser.password)
        viewModel.createAccount(
            onSuccess = { viewModel.toggleSuccessDisplay(true) },
            onError = { viewModel.toggleErrorDisplay(true) }
        )
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.showSuccess.value == true)
        assertFalse(viewModel.showError.value == true)
    }

    @Test
    fun `createAccount should handle registration error`() = runTest {
        // Arrange
        val dummyUser = User("Yohanes Taddese", "yohanes.taddese@example.com", "password123")
        val exceptionMessage = "Network error"
        coEvery { todoApiService.register(any(), dummyUser) } throws Exception(exceptionMessage)
        coEvery { context.getString(R.string.account_creation_error) } returns "Account creation error: "

        // Act
        viewModel.updateUsername(dummyUser.name)
        viewModel.updateEmail(dummyUser.email)
        viewModel.updatePassword(dummyUser.password)
        viewModel.createAccount(
            onSuccess = { viewModel.toggleSuccessDisplay(false) },
            onError = { viewModel.toggleErrorDisplay(true) }
        )
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.showError.value == true)
        assertTrue(viewModel.errorMessage.value?.contains("Account creation error: $exceptionMessage") == true)
    }
}
