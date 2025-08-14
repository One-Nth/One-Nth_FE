package com.example.onenthapp.data.transaction

import com.example.onenthapp.RetrofitInstance

class TransactionRepository {
    private val api = RetrofitInstance.transactionApi

    suspend fun getAvailableProducts() =
        api.getAvailableProducts()

    suspend fun getDealConfirmationForm(roomName: String) =
        api.getDealConfirmationForm(roomName)

    suspend fun confirmationTransaction(roomName: String, request: DealCompletionRequest) =
        api.confirmationTransaction(roomName, request)

    suspend fun completeTransaction(roomName: String, request: CompleteTransactionRequest) =
        api.completeTransaction(roomName, request)

    suspend fun cancelTransaction(roomName: String, request: CancelTransactionRequest) =
        api.cancelTransaction(roomName, request)

    suspend fun getMyHistory(reviewStatus: String) =
        api.getMyHistory(reviewStatus)
}
