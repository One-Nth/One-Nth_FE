package com.example.onenthapp.data.alarm

data class FcmTokenRequest(
    val fcmToken: String
)

data class FcmTokenResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String
)
