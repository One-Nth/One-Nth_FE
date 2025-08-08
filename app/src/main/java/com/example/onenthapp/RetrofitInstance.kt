package com.example.onenthapp

import android.util.Log
import com.example.onenthapp.data.AuthApi
import com.example.onenthapp.data.PlusApi
import com.example.onenthapp.data.chat.MessageApi
import com.example.onenthapp.data.userset.UserSetApi
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
        val token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzUzOTMxOTA1LCJleHAiOjE3NTM5NDYzMDV9.lHmj0PCmu5B-6vJ5M3NAj1_N2OaZEqxzJoKi2jZqVH0"
        Log.d("RetrofitInstance", "현재 토큰: $token")
        return token
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

    val messageApi: MessageApi by lazy {
        retrofit.create(MessageApi::class.java)
    }
    val usersetApi: UserSetApi by lazy {
        retrofit.create(UserSetApi::class.java)
    }
}
