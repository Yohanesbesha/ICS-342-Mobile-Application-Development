package edu.yohanes.todolistapp

import android.content.Context
import android.content.SharedPreferences


interface PreferHelper {
    fun saveUserinfo(userID: String)
    fun getUserID(): String?
    fun saveToken(token: String)
    fun getToken(): String?
    fun saveTodo_id(todoID: String)
    fun getTodo_id(): String?
}

class SharedPref(context: Context) : PreferHelper {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "user_prefs"
    }

    override fun saveUserinfo(userID: String) {
        sharedPreferences.edit().putString("userID", userID).apply()
    }

    override fun getUserID(): String? {
        return sharedPreferences.getString("userID", "")
    }

    override fun saveToken(token: String) {
        sharedPreferences.edit().putString("token", token).apply()
    }

    override fun getToken(): String? {
        return sharedPreferences.getString("token", "")
    }

    override fun saveTodo_id(todoID: String) {
        sharedPreferences.edit().putString("todoID", todoID).apply()
    }

    override fun getTodo_id(): String? {
        return sharedPreferences.getString("todoID", null)
    }


}
