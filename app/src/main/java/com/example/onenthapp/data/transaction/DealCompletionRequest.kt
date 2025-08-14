package com.example.onenthapp.data.transaction

data class DealCompletionRequest(
    val itemId: Int,
    val itemType: String, // "PURCHASE"
    val dealDate: String, // "YYYY-MM-DD"
    val tradeType: String, // "IN_PERSON" 등
    val purchasePrice: Int,
    val originalPrice: Int,
)
