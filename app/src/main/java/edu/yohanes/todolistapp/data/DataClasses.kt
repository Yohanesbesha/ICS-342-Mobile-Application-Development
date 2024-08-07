package edu.yohanes.todolistapp.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TodoItem(
    @Json(name = "id") val id: String,
    @Json(name = "description") val description: String,
    @Json(name = "completed") var completed: Boolean,
)

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class UserResponse(
    @Json(name = "token") val token: String,
    @Json(name = "id") val userId: String,
    @Json(name = "name") val name :String
)

data class TodoItemResponse(
    @Json(name = "id") val id: String,
    @Json(name = "description") val description: String,
    @Json(name = "completed") var completed: Int,
)
