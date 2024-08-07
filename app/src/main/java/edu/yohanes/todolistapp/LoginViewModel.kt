package edu.yohanes.todolistapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.yohanes.todolistapp.data.User
import kotlinx.coroutines.launch

class LoginViewModel(
    private val sharedPreferences: SharedPref,
    private val todoApiService: TodoApiService,
    private val context: Context
) : ViewModel() {
    private val apiKey = "13c0f85d-69c5-41d6-81c5-9192362305aa"

    private val loginEmail = MutableLiveData("")
    val email: LiveData<String> = loginEmail

    private val loginPassword = MutableLiveData("")
    val password: LiveData<String> = loginPassword

    private val loginError = MutableLiveData(false)
    val showError: LiveData<Boolean> = loginError

    private val loginErrorPayload = MutableLiveData("")
    val errorMessage: LiveData<String> = loginErrorPayload

    fun updateEmail(newEmail: String) {
        loginEmail.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        loginPassword.value = newPassword
    }

    fun updateShowError(showErrorChange: Boolean) {
        loginError.value = showErrorChange
    }

    fun errorPayload(showErrorMessage: String) {
        loginErrorPayload.value = showErrorMessage
    }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (loginEmail.value.isNullOrBlank() || loginPassword.value.isNullOrBlank()) {
            loginErrorPayload.value = context.getString(R.string.empty_fields_error)
            loginError.value = true
            return
        }

        viewModelScope.launch {
            try {
                val response = todoApiService.login(apiKey, User("", loginEmail.value!!, loginPassword.value!!))
                sharedPreferences.saveUserinfo(response.userId)
                sharedPreferences.saveToken(response.token)
                onSuccess()
            } catch (e: Exception) {
                loginErrorPayload.value = context.getString(R.string.loginFailed)
                loginError.value = true
                onError(context.getString(R.string.loginFailed))
            }
        }
    }
}
