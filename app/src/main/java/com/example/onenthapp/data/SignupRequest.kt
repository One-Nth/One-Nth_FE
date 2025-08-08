package com.example.onenthapp.data

import com.google.gson.annotations.SerializedName

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val nickname: String,
    val regionName: String,
    val marketingAgree: Boolean
)

data class SignupResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: SignupResult
)

data class SignupResult(
    val memberId: Int,
    val createdAt: String
)

// 서버가 회원가입 후 바로 JWT를 내려주는 스펙 가정
data class LocalSignupResult(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    val memberId: Long?
)