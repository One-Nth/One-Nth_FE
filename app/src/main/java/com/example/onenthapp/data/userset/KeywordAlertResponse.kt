package com.example.onenthapp.data.userset

data class KeywordAlertResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: KeywordAlertResult
)
