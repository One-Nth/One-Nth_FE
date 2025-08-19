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
        val puchaseReviewId: Long?,      // 구매 후기 응답용
        val sharingReviewId: Long?       // 공유 후기 응답용
    )
}

data class ReviewImage(
    val id: Long,
    val url: String
)

data class DeleteReviewImageRequest(
    val imageIds: List<Long>
)
