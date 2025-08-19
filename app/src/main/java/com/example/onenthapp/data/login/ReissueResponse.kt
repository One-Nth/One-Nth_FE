package com.example.onenthapp.data.login

data class ReissueResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: ReissueResult
)

data class ReissueResult(
    val accessToken: String,
    val refreshToken: String, // 혹시 재발급되면 이 값도 저장 가능
    val memberId: Int
)