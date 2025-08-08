package com.example.onenthapp.data.userset

data class AlertToggleResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: AlertToggleResult
)
