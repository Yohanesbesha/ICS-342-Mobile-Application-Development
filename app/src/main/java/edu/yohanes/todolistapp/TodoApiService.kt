package edu.yohanes.todolistapp

import edu.yohanes.todolistapp.data.TodoItem
import edu.yohanes.todolistapp.data.TodoItemReceiver
import edu.yohanes.todolistapp.data.User
import edu.yohanes.todolistapp.data.UserResponse
import retrofit2.http.*

interface TodoApiService {

    @GET("/api/users/{user_id}/todos")
    suspend fun getTodos(
        @Header("Authorization") bearerToken: String,
        @Path("user_id") userId: String,
        @Query("apikey") apiKey: String,
    ): List<TodoItemReceiver>

    @POST("/api/users/{user_id}/todos")
    suspend fun createTodo(
        @Header("Authorization") bearerToken: String,
        @Path("user_id") userId: String,
        @Query("apikey") apiKey: String,
        @Body newTodo: TodoItem
    ): TodoItem

    @PUT("/api/users/{user_id}/todos/{id}")
    suspend fun updateTodo(
        @Path("user_id") userId: String,
        @Path("id") todoId: String,
        @Query("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body updatedTodo: TodoItem
    ): TodoItem

    @POST("/api/users/register")
    suspend fun register(
        @Query("apikey") apiKey: String,
        @Body user: User
    ): UserResponse

    @POST("/api/users/login")
    suspend fun login(
        @Query("apikey") apiKey: String,
        @Body user: User
    ): UserResponse
}
