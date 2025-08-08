package com.example.onenthapp.data.chat

data class ChatNameRequest(
    val targetMemberId: Int,
    val chatRoomType: String
)