package com.example.onenthapp.data

data class EmailResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: T?
)
