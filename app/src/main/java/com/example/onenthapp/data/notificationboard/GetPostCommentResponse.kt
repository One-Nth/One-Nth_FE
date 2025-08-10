package com.example.onenthapp.data.notificationboard

data class GetPostCommentsResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<CommentItem>
)

data class CommentItem(
    val id: Int,
    val content: String,
    val nickname: String,
    val memberId: Int,
    val createdAt: String
)

