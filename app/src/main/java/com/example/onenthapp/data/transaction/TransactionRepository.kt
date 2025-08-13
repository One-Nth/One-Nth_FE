package com.example.onenthapp.data.transaction

import com.example.onenthapp.RetrofitInstance

class TransactionRepository {
    private val api = RetrofitInstance.transactionApi

    suspend fun getAvailableProducts(): Result<List<AvailableProduct>> = try {
        val response = api.getAvailableProducts()
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.isSuccess == true) Result.success(body.result ?: emptyList())
            else Result.failure(Exception(body?.message ?: "상품 조회 실패"))
        } else {
            Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createDealConfirmationForm(transactionId: Int, request: DealConfirmationFormRequest): Result<String> = try {
        val response = api.createDealConfirmationForm(transactionId, request)
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.isSuccess == true) Result.success(body.result ?: "")
            else Result.failure(Exception(body?.message ?: "폼 발행 실패"))
        } else {
            Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun cancelTransaction(transactionId: Int, request: CancelTransactionRequest): Result<String> = try {
        val response = api.cancelTransaction(transactionId, request)
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.isSuccess == true) Result.success(body.result ?: "")
            else Result.failure(Exception(body?.message ?: "취소 실패"))
        } else {
            Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun completeTransaction(transactionId: Int, request: CompleteTransactionRequest): Result<String> = try {
        val response = api.completeTransaction(transactionId, request)
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.isSuccess == true) Result.success(body.result ?: "")
            else Result.failure(Exception(body?.message ?: "거래 완료 실패"))
        } else {
            Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
