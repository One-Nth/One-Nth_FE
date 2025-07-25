package com.example.onenthapp.model

import java.io.Serializable

data class TipItem(
    val title: String,
    val content: String,
    val timeAgo: String,
    val commentCount: Int,
    val likeCount: Int,
    val viewCount: Int
) : Serializable
