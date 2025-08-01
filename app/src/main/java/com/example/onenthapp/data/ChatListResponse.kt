package com.example.onenthapp.data

data class ChatListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<ChatRoom>
)

data class ChatRoom(
    val chatRoomId: Int,
    val chatRoomType: String, // 예: "DEAL"
    val chatRoomName: String,
    val opponentId: Int,
    val lastMessageContent: String,
    val lastMessageTime: String // ISO8601 형식, e.g. "2025-08-01T12:02:15.248Z"
)
