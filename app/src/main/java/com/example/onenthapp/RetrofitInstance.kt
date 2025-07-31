package com.example.onenthapp

import com.example.onenthapp.data.AuthApi
import com.example.onenthapp.data.PlusApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitInstance {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor{ chain ->
            val orig = chain.request()
            val req  = orig.newBuilder()
                .addHeader("Authorization", "Bearer ${getToken()}")
                .build()
            chain.proceed(req)
        }
        .addInterceptor(logging)
        .build()

    private fun getToken(): String {
        return "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzUzOTMxOTA1LCJleHAiOjE3NTM5NDYzMDV9.lHmj0PCmu5B-6vJ5M3NAj1_N2OaZEqxzJoKi2jZqVH0"
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://43.201.21.163:8080/api/") // 서버 주소
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
    val plusApi: PlusApi by lazy {
        retrofit.create(PlusApi::class.java)
    }
}