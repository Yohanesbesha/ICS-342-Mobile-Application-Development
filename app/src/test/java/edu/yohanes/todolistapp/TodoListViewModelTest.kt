package edu.yohanes.todolistapp

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.yohanes.todolistapp.data.TodoItem
import edu.yohanes.todolistapp.data.TodoItemResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class TodoListViewModelTest {

    @get:Rule
    var instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private lateinit var viewModel: TodoListViewModel
    private val sharedPreferences: SharedPref = mockk(relaxed = true)
    private val todoApiService: TodoApiService = mockk()
    private val context: Context = mockk(relaxed = true)
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = TodoListViewModel(sharedPreferences, todoApiService, context)
        viewModel.todosList.value = mutableListOf()  // Initialize LiveData to prevent null issues
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()  // Reset the dispatcher after tests to clean up
    }

    @Test
    fun `initial state should have no todos and no errors`() {
        assertTrue(viewModel.todos.value.isNullOrEmpty())
        assertFalse(viewModel.showError.value!!)
        assertEquals("", viewModel.errorMessage.value)
    }

    @Test
    fun `loadTodos should load and update todos successfully`() = runTest {
        val todosResponse = listOf(
            TodoItemResponse("1", "Test Todo 1", 0),
            TodoItemResponse("2", "Test Todo 2", 1))

        coEvery { sharedPreferences.getToken() } returns "God1"
        coEvery { sharedPreferences.getUserID() } returns "God1"
        coEvery { todoApiService.getTodos(any(), any(), any()) } returns todosResponse

        viewModel.loadTodos()
        advanceUntilIdle()

        val result = viewModel.todos.value
        assertEquals(2, result?.size)
        assertFalse(result?.find { it.id == "1" }?.completed ?: true)
        assertTrue(result?.find { it.id == "2" }?.completed ?: false)
        assertFalse(viewModel.showError.value!!)
    }

    @Test
    fun `loadTodos should handle API errors`() = runTest {
        coEvery { sharedPreferences.getToken() } returns "God1"
        coEvery { sharedPreferences.getUserID() } returns "God1"
        coEvery { todoApiService.getTodos(any(), any(), any()) } throws Exception("Failed to load")

        viewModel.loadTodos()
        advanceUntilIdle()

        assertTrue(viewModel.showError.value!!)
        assertTrue(viewModel.errorMessage.value?.contains("Failed to load") == true)
    }

    @Test
    fun `addTodo should add a new todo successfully`() = runTest {
        val newTodo = TodoItem("1", "New Todo", false)
        coEvery { sharedPreferences.getToken() } returns "God1"
        coEvery { sharedPreferences.getUserID() } returns "God11"
        coEvery { todoApiService.createTodo(any(), any(), any(), any()) } returns newTodo

        viewModel.addTodo(newTodo)
        advanceUntilIdle()

        val result = viewModel.todos.value
        assertEquals(1, result?.size)
        assertTrue(result?.contains(newTodo) == true)
        assertFalse(viewModel.showError.value!!)
    }

    @Test
    fun `addTodo should handle API errors`() = runTest {
        val newTodo = TodoItem("1", "New Todo", false)
        coEvery { sharedPreferences.getToken() } returns "God11"
        coEvery { sharedPreferences.getUserID() } returns "God11"
        coEvery { todoApiService.createTodo(any(), any(), any(), any()) } throws Exception("Failed to add")

        viewModel.addTodo(newTodo)
        advanceUntilIdle()

        assertTrue(viewModel.showError.value!!)
        assertTrue(viewModel.errorMessage.value?.contains("Failed to add") == true)
    }

    @Test
    fun `updateCheckBox should update the completed status of a todo item successfully`() = runTest {
        val todo = TodoItem("1", "Test Todo", false)
        val updatedTodo = todo.copy(completed = true)
        coEvery { sharedPreferences.getToken() } returns "God1"
        coEvery { sharedPreferences.getUserID() } returns "God11"
        coEvery { todoApiService.updateTodo(any(), any(), any(), any(), updatedTodo) } returns updatedTodo

        viewModel.todosList.value = listOf(todo)  // Prepopulate the todos list
        viewModel.updateCheckBox(todo)
        advanceUntilIdle()

        val result = viewModel.todos.value?.find { it.id == todo.id }
        assertTrue("The todo item should be marked as completed", result?.completed ?: false)
        assertFalse(viewModel.showError.value!!)
    }

    @Test
    fun `updateCheckBox should handle API errors`() = runTest {
        val todo = TodoItem("1", "Test Todo", false)
        coEvery { sharedPreferences.getToken() } returns "God1"
        coEvery { sharedPreferences.getUserID() } returns "God11"
        coEvery { todoApiService.updateTodo(any(), any(), any(), any(), any()) } throws Exception("Failed to update")

        viewModel.todosList.value = listOf(todo)  // Prepopulate the todos list
        viewModel.updateCheckBox(todo)
        advanceUntilIdle()

        assertTrue(viewModel.showError.value!!)
        assertTrue(viewModel.errorMessage.value?.contains("Failed to update") == true)
    }
}
