package com.example.onenthapp.data.chat

data class LeaveChatResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String
)