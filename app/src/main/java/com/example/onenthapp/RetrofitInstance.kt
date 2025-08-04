package com.example.onenthapp

import com.example.onenthapp.data.AuthApi
import com.example.onenthapp.data.MemberApi
import com.example.onenthapp.data.PlusApi
import com.example.onenthapp.data.MessageApi
import com.example.onenthapp.util.TokenManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitInstance {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url.toString()

            // 로그인 또는 소셜 회원가입 요청이 아닌 경우에만 Authorization 헤더 추가
            val requestBuilder = originalRequest.newBuilder()
            if (!originalUrl.contains("/auth/kakao/login") && !originalUrl.contains("/auth/kakao/signup")) {
                val token = TokenManager.getToken()
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val modifiedRequest = requestBuilder.build()
            chain.proceed(modifiedRequest)
        }
        .addInterceptor(logging)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://43.201.21.163:8080/api/")
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

    val memberApi: MemberApi by lazy {
        retrofit.create(MemberApi::class.java)
    }
}
