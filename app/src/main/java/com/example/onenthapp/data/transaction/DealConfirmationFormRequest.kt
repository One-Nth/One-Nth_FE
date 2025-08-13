package com.example.onenthapp.data.transaction

data class DealConfirmationFormRequest(
    val itemId: Int,
    val itemType: String,         // PURCHASE or SHARE
    val dealDate: String,         // yyyy-MM-dd
    val tradeType: String,        // IN_PERSON or DELIVERY
    val purchasePrice: Int,
    val originalPrice: Int
)
