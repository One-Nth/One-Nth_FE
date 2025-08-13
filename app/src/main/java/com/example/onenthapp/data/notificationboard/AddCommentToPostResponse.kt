package com.example.onenthapp.data.notificationboard

data class AddCommentToPostResponse(
    val isSuccess: Boolean,
    val code : String,
    val message : String,
    val result : Comment,
)
