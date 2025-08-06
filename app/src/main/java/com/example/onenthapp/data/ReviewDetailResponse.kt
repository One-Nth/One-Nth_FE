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
    val content: String,
    val rate: Int,
    val reviewImageList: List<String>
)

