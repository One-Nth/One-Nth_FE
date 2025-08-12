package com.example.onenthapp.data.nwonsaved

data class LookNwonSavedResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: LookNwonSavedResult
)

data class LookNwonSavedResult(
    val totalReviewCount: Int,        // 받은 리뷰 수
    val totalReviewRating: Double,    // 받은 리뷰 별점 통계
    val savedAmount: Int,             // N원 아꼈어요
    val totalDealHistory: DealHistory,      // N분의 1 거래
    val purchaseDealHistory: DealHistory,   // 같이 사요
    val shareDealHistory: DealHistory       // 함께 나눠요
)

data class DealHistory(
    val totalDealCount: Int,   // 총 거래 건수
    val totalDealAmount: Int   // 총 거래 금액
)
