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

    private val todosList = MutableLiveData<List<TodoItem>>()
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
                val todos = todoApiService.getTodos(bToken, userID!!, apiKey)
                val todoList = todos.map { item ->
                    val completed = item.completed == 1
                    TodoItem(item.id, item.description, completed)
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
                val todoID = todo.id
                val updatedCompleted = !todo.completed
                todoApiService.updateTodo(userID!!, todoID, apiKey, bToken, todo.copy(completed = updatedCompleted))
                todosList.value = todosList.value?.map { item ->
                    if (item.id == todoID) item.copy(completed = updatedCompleted) else item
                }
            } catch (e: Exception) {
                errorPayload.value = context.getString(R.string.failedUpdatedCheck) + "${e.message}"
                displayError.value = true
            }
        }
    }
}
