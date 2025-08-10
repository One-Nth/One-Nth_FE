package com.example.onenthapp.data.post

data class SearchPostResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<SearchPostDto>
)

data class SearchPostDto(
    val postId: Long,
    val title: String,
    val contentPreview: String,
    val commentCount: Int,
    val likeCount: Int,
    val viewCount: Int,
    val scrapStatus: Boolean,
    val createdAt: String
)

