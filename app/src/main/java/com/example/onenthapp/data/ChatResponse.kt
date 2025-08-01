package com.example.onenthapp.data

data class ChatResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<ChatMessage>
)