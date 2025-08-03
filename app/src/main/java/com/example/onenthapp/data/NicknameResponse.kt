package com.example.onenthapp.data

data class NicknameResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: NicknameResult
)

data class NicknameResult(
    val nickname: String
)
