package com.example.onenthapp.data.chat

data class MemberNickName(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Member
)

data class Member(
    val memberId: Int,
    val nickname: String,
    val profileImageUrl: String? // null 허용
)

