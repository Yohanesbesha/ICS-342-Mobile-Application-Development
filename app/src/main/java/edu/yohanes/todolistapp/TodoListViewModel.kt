package edu.yohanes.todolistapp

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.yohanes.todolistapp.data.TodoItem
import kotlinx.coroutines.launch

class TodoListViewModel @SuppressLint("StaticFieldLeak") constructor(
    private val sharedPreferences: SharedPref,
    private val todoApiService: TodoApiService,
    @SuppressLint("StaticFieldLeak") private val context: Context
) : ViewModel() {
    private val apiKey = "13c0f85d-69c5-41d6-81c5-9192362305aa"

    val todosList = MutableLiveData<List<TodoItem>>()
    val todos: LiveData<List<TodoItem>> = todosList

    private val displayError = MutableLiveData(false)
    val showError: LiveData<Boolean> = displayError

    private val errorPayload = MutableLiveData("")
    val errorMessage: LiveData<String> = errorPayload

    fun displayUpdatedErrorLog(showErrorChange: Boolean) {
        displayError.value = showErrorChange
    }

    fun errorContent(showErrorMessage: String) {
        errorPayload.value = showErrorMessage
    }

    fun loadTodos() {
        viewModelScope.launch {
            try {
                val bToken = "Bearer " + sharedPreferences.getToken()
                val userID = sharedPreferences.getUserID()
                val todosResponse = todoApiService.getTodos(bToken, userID!!, apiKey)

                // Map the TodoItemResponse to TodoItem
                val todoList = todosResponse.map { response ->
                    TodoItem(
                        id = response.id,
                        description = response.description,
                        completed = response.completed == 1 // Assuming 1 means completed
                    )
                }
                todosList.value = todoList
            } catch (e: Exception) {
                errorPayload.value = context.getString(R.string.failedLoadTodos) + "${e.message}"
                displayError.value = true
            }
        }
    }


    fun addTodo(todo: TodoItem) {
        viewModelScope.launch {
            try {
                val bToken = "Bearer " + sharedPreferences.getToken()
                val userID = sharedPreferences.getUserID()
                val response = todoApiService.createTodo(bToken, userID!!, apiKey, todo)
                val newList = todosList.value.orEmpty() + response
                todosList.value = newList
            } catch (e: Exception) {
                errorPayload.value = context.getString(R.string.failedAddTodo) + "${e.message}"
                displayError.value = true
            }
        }
    }

    fun updateCheckBox(todo: TodoItem) {
        viewModelScope.launch {
            try {
                val bToken = "Bearer " + sharedPreferences.getToken()
                val userID = sharedPreferences.getUserID()
                val updatedCompleted = !todo.completed
                val updatedTodo = todo.copy(completed = updatedCompleted)
                val result = todoApiService.updateTodo(userID!!, todo.id, apiKey, bToken, updatedTodo)

                // Update the LiveData with the updated list
                todosList.value = todosList.value?.map { item ->
                    if (item.id == todo.id) result else item
                }
            } catch (e: Exception) {
                errorPayload.value = context.getString(R.string.failedUpdatedCheck) + "${e.message}"
                displayError.value = true
            }
        }
    }





}
