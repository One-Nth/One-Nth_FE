package com.example.onenthapp.data

data class UserReviewResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: UserReviewResult
)

data class UserReviewResult(
    val memberId: Long,
    val memberNickName: String?,     // ✅ 추가(옵션)
    val profileImageUrl: String?,    // ✅ 추가(옵션)
    val reviewList: List<Review>
)

data class Review(
    val reviewId: Long,
    val itemType: String,
    val itemId: Long,
    val createdAt: String,
    val reviewerId: Long,
    val reviewerNickName: String,          // ✅ 추가
    val reviewerProfileImageUrl: String?,  // ✅ 추가
    val reviewTargetId: Long,
    val content: String,
    val rate: Int,
    val reviewImageList: List<String>
)
