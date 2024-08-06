package edu.yohanes.todolistapp

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object Retrofit {
    private const val BASE_URL = "https://todos.simpleapi.dev/"
    private val okHttpClient = OkHttpClient.Builder().build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()

    val todoApiService: TodoApiService by lazy {
        retrofit.create(TodoApiService::class.java)
    }
}
