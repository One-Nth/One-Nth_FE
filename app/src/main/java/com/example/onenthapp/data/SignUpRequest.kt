package com.example.onenthapp.data

data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val nickname: String,
    val regionName: String,
    val marketingAgree: Boolean
)

data class SignUpResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: SignUpResult?
)

data class SignUpResult(
    val memberId: Int,
    val createdAt: String
)
