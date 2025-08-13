package com.example.onenthapp.data.notificationboard

data class ScrapPostResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: ScrapResult
)

data class ScrapResult(
    val isSuccess: Boolean
)

