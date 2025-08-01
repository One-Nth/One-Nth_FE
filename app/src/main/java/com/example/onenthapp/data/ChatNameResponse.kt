package com.example.onenthapp.data

data class ChatNameResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: ChatNameResult
)

data class ChatNameResult(
    val chatRoomName: String,
    val chatRoomId: Long
)
