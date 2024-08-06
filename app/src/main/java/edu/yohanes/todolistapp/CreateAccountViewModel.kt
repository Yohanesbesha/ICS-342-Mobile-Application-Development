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
    private val sharedPreferences: SharedPreferencesHelper,
    private val todoApiService: TodoApiService,
    @SuppressLint("StaticFieldLeak") private val context: Context
) : ViewModel() {
    private val apiKey = "13c0f85d-69c5-41d6-81c5-9192362305aa"

    private val _name = MutableLiveData("")
    val name: LiveData<String> = _name

    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password

    private val _showError = MutableLiveData(false)
    val showError: LiveData<Boolean> = _showError

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> = _errorMessage

    private val _showSuccess = MutableLiveData(false)
    val showSuccess: LiveData<Boolean> = _showSuccess

    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onShowErrorChange(showErrorChange: Boolean) {
        _showError.value = showErrorChange
    }

    fun onErrorMessage(showErrorMessage: String) {
        _errorMessage.value = showErrorMessage
    }

    fun onShowSuccessChange(showSuccessChange: Boolean) {
        _showSuccess.value = showSuccessChange
    }

    fun clearError() {
        _showError.value = false
    }

    fun clearSuccess() {
        _showSuccess.value = false
    }

    fun createAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_email.value.isNullOrBlank() || _password.value.isNullOrBlank() || _name.value.isNullOrBlank()) {
            _errorMessage.value = context.getString(R.string.create_account_empty_fields_error)
            _showError.value = true
            return
        }

        viewModelScope.launch {
            try {
                val response = todoApiService.register(apiKey, User(_name.value!!, _email.value!!, _password.value!!))
                sharedPreferences.saveUserinfo(response.userId)
                sharedPreferences.saveToken(response.token)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.account_creation_error) + "${e.message}"
                _showError.value = true
                onError(context.getString(R.string.account_creation_error) + "${e.message}")
            }
        }
    }
}
