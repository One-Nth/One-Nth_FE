package com.example.onenthapp

import android.util.Log
import com.example.onenthapp.data.AuthApi
import com.example.onenthapp.data.MemberApi
import com.example.onenthapp.data.PlusApi
import com.example.onenthapp.data.userset.UserSetApi
import com.example.onenthapp.data.chat.MessageApi
import com.example.onenthapp.data.ReviewApi
import com.example.onenthapp.data.notificationboard.NotificationboardApi
import com.example.onenthapp.data.transaction.TransactionApi
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

            // 토큰 가져오기 (로그 추가)
            val token = TokenManager.getToken()
            Log.d("토큰확인", "TokenManager.getToken(): $token")
            Log.d("Retrofit", "Calling $originalUrl with token: Bearer $token")

            val requestBuilder = originalRequest.newBuilder()

            // 로그인/회원가입 제외하고 Authorization 헤더 추가
            if (!originalUrl.contains("/auth/kakao/login") && !originalUrl.contains("/auth/kakao/signup")) {
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
    
    val usersetApi: UserSetApi by lazy {
        retrofit.create(UserSetApi::class.java)
    }

    val memberApi: MemberApi by lazy {
        retrofit.create(MemberApi::class.java)
    }

    val reviewApi: ReviewApi by lazy {
        retrofit.create(ReviewApi::class.java)
    }

    val transactionApi: TransactionApi by lazy {
        retrofit.create(TransactionApi::class.java)
    }

    val notificationboardApi: NotificationboardApi by lazy {
        retrofit.create(NotificationboardApi::class.java)
    }

}
