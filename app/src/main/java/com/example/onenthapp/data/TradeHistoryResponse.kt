package com.example.onenthapp.data

data class TradeHistoryResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: TradeHistory
)

data class TradeHistory(
    val userId: Long,
    val reviewCount: Int,
    val totalRating: Float,
    val totalDealsCount: Int
)
