package com.example.onenthapp.feature.alarm

data class AlarmItem(
    val type: String,
    val message: String,
    val timeAgo: String,
    val navigationImageResId: Int,
    val isRead: Boolean,
    val postId: Int? = null
)
