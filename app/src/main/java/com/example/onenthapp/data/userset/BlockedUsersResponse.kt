package com.example.onenthapp.data.userset

data class BlockedUsersResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: BlockedUserList
)
