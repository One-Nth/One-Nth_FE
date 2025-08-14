package com.example.onenthapp.data.chat

data class ChatMessage(
    val senderMemberId: Int,
    val content: String,
    val messageTime: String  // ISO‑8601 형식
)