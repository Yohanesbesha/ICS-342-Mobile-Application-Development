package edu.yohanes.todolistapp

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.yohanes.todolistapp.data.TodoItem
import kotlinx.coroutines.launch

class ViewModelFactories @SuppressLint("StaticFieldLeak") constructor(
    private val sharedPreferences: SharedPreferencesHelper,
    private val todoApiService: TodoApiService,
    @SuppressLint("StaticFieldLeak") private val context: Context
) : ViewModel() {
    private val apiKey = "13c0f85d-69c5-41d6-81c5-9192362305aa"

    private val _todos = MutableLiveData<List<TodoItem>>()
    val todos: LiveData<List<TodoItem>> = _todos

    private val _showError = MutableLiveData(false)
    val showError: LiveData<Boolean> = _showError

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> = _errorMessage

    fun onShowErrorChange(showErrorChange: Boolean) {
        _showError.value = showErrorChange
    }

    fun onErrorMessage(showErrorMessage: String) {
        _errorMessage.value = showErrorMessage
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
                _todos.value = todoList
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.failedLoadTodos) + "${e.message}"
                _showError.value = true
            }
        }
    }

    fun addTodo(todo: TodoItem) {
        viewModelScope.launch {
            try {
                val bToken = "Bearer " + sharedPreferences.getToken()
                val userID = sharedPreferences.getUserID()
                val response = todoApiService.createTodo(bToken, userID!!, apiKey, todo)
                val newList = _todos.value.orEmpty() + response
                _todos.value = newList
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.failedAddTodo) + "${e.message}"
                _showError.value = true
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
                _todos.value = _todos.value?.map { item ->
                    if (item.id == todoID) item.copy(completed = updatedCompleted) else item
                }
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.failedUpdatedCheck) + "${e.message}"
                _showError.value = true
            }
        }
    }
}
