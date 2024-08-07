package edu.yohanes.todolistapp

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.yohanes.todolistapp.data.User
import kotlinx.coroutines.launch

class CreateAccountViewModel(
    private val sharedPreferences: SharedPref,
    private val todoApiService: TodoApiService,
    @SuppressLint("StaticFieldLeak") private val context: Context
) : ViewModel() {
    private val apiKey = "13c0f85d-69c5-41d6-81c5-9192362305aa"

    private val userName = MutableLiveData("")
    val name: LiveData<String> = userName

    private val userEmail = MutableLiveData("")
    val email: LiveData<String> = userEmail

    private val userPassword = MutableLiveData("")
    val password: LiveData<String> = userPassword

    private val throwError = MutableLiveData(false)
    val showError: LiveData<Boolean> = throwError

    private val errorPayload = MutableLiveData("")
    val errorMessage: LiveData<String> = errorPayload

    private val successCase = MutableLiveData(false)
    val showSuccess: LiveData<Boolean> = successCase

    fun updateUsername(newName: String) {
        userName.value = newName
    }

    fun updateEmail(newEmail: String) {
        userEmail.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        userPassword.value = newPassword
    }

    fun toggleErrorDisplay(showErrorChange: Boolean) {
        throwError.value = showErrorChange
    }

    fun updateErrorText(showErrorMessage: String) {
        errorPayload.value = showErrorMessage
    }

    fun toggleSuccessDisplay(showSuccessChange: Boolean) {
        successCase.value = showSuccessChange
    }

    fun clearErrorDisplay() {
        throwError.value = false
    }

    fun clearSuccessDisplay() {
        successCase.value = false
    }

    fun createAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (userEmail.value.isNullOrBlank() || userPassword.value.isNullOrBlank() || userName.value.isNullOrBlank()) {
            errorPayload.value = context.getString(R.string.create_account_empty_fields_error)
            throwError.value = true
            return
        }

        viewModelScope.launch {
            try {
                val response = todoApiService.register(apiKey, User(userName.value!!, userEmail.value!!, userPassword.value!!))
                sharedPreferences.saveUserinfo(response.userId)
                sharedPreferences.saveToken(response.token)
                onSuccess()
            } catch (e: Exception) {
                errorPayload.value = context.getString(R.string.account_creation_error) + "${e.message}"
                throwError.value = true
                onError(context.getString(R.string.account_creation_error) + "${e.message}")
            }
        }
    }
}
