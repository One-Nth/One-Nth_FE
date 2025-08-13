package com.example.onenthapp.data

data class ReviewDetailResponse(
    val isSuccess: Boolean,
    val message: String,
    val result: ReviewDetailResult
)

data class ReviewDetailResult(
    val reviewId: Long,
    val itemType: String,
    val itemId: Long,
    val itemTitle: String,
    val content: String,
    val rate: Int,
    val reviewImageList: List<ReviewImageDto>  // 수정!
)

data class ReviewImageDto(
    val reviewImageId: Long,
    val imageUrl: String
)
