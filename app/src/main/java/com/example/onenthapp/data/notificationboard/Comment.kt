package com.example.onenthapp.data.notificationboard

data class Comment(
    val id : Int,
    val content: String,
    val nickname: String,
    val memberId: Int,
    val createdAt: String,
)
