package com.example.onenthapp.data.post

// TipItem.kt
data class TipItem(
    val postId: Long,
    val title: String,
    val content: String,
    val timeAgo: String,
    val commentCount: Int,
    val likeCount: Int,
    val viewCount: Int,
    val imageUrls: List<String> = emptyList(),

    // ✅ 추가
    val likedByMe: Boolean = false,
    val scrappedByMe: Boolean = false
)

