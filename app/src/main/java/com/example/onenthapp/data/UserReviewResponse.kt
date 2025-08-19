package com.example.onenthapp.data

data class UserReviewResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: UserReviewResult
)

data class UserReviewResult(
    val memberId: Long,
    val memberNickName: String?,     // ✅ 추가
    val profileImageUrl: String?,    // ✅ 추가
    val reviewList: List<Review>
)

data class Review(
    val reviewId: Long,
    val itemType: String,
    val itemId: Long,
    val itemTitle: String,
    val createdAt: String,
    val reviewerId: Long,
    val reviewerNickName: String,          // ✅ 추가
    val reviewerProfileImageUrl: String?,  // ✅ 추가
    val reviewTargetId: Long,
    val content: String,
    val rate: Float,
    val reviewImageList: List<String>
)
