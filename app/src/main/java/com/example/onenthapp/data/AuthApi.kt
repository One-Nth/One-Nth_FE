package com.example.onenthapp.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("email-auth/request-code")
    suspend fun requestCode(
        @Body body: Map<String, String>
    ): Response<EmailResponse<String>>


    @POST("email-auth/verify-code")
    suspend fun verifyCode(
        @Body body: Map<String, String>
    ): Response<EmailResponse<String>>

    // ✅ 회원가입 API 추가
    @POST("/api/members/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<SignupResponse>

    @POST("/api/members/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}
