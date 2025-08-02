package com.example.onenthapp.data

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

