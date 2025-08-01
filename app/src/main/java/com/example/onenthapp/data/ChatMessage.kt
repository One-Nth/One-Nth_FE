package com.example.onenthapp.data

data class ChatMessage(
    val senderMemberId: Long,
    val content: String,
    val messageTime: String  // ISO‑8601 형식
)