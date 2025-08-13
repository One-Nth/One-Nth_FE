package com.example.onenthapp.data.transaction

data class CompleteTransactionRequest(
    val dealConfirmationId: Int,
    val dealDate: String,
    val tradePrice: Int,
    val tradeCount: Int,
    val tradeType: String
)
