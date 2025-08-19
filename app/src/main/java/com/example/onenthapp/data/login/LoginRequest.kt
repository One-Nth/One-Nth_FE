package com.example.onenthapp.data.login

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: LoginResult
)

data class LoginResult(
    val memberId: Int,
    val accessToken: String,
    val refreshToken: String
)

