package com.example.onenthapp.data.transaction

data class CancelTransactionRequest(
    val dealConfirmationId: Int,
    val cancelReason: String
)