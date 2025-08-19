package com.example.onenthapp.data

data class MemberProfileResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: MemberProfile?
)

data class MemberProfile(
    val memberId: Long,
    val nickname: String?,
    val profileImageUrl: String?
)

