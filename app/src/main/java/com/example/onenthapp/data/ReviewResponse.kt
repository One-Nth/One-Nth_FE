package com.example.onenthapp.data

data class ReviewBody(
    val content: String,
    val rate: Float
)

data class ReviewResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: ResultData
) {
    data class ResultData(
        val puchaseReviewId: Long
    )
}

