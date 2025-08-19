package com.example.onenthapp.data

data class MyReview(
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

data class MyReviewResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: MyReviewResult
)

data class MyReviewResult(
    val memberId: Long,
    val reviewList: List<MyReview>
)

