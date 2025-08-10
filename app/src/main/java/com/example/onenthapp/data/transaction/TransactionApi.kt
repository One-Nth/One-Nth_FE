package com.example.onenthapp.data.transaction

import com.example.onenthapp.data.CommonResponse
import retrofit2.Response
import retrofit2.http.*

interface TransactionApi {

    // 1. 거래 취소
    @POST("/api/transactions/{transactionId}/completion")
    suspend fun cancelTransaction(
        @Path("transactionId") transactionId: Int,
        @Body request: CancelTransactionRequest
    ): Response<CommonResponse<String>>

    // 5. 거래 가능한 상품 조회
    @GET("/api/deals/available-products")
    suspend fun getAvailableProducts(): Response<AvailableProductsResponse>

    // 3. 거래 확정 폼 발행
    @POST("/api/transactions/{transactionId}/confirmation-form")
    suspend fun createDealConfirmationForm(
        @Path("transactionId") transactionId: Int,
        @Body request: DealConfirmationFormRequest
    ): Response<CommonResponse<String>>


    // 2. 거래 완료 폼 발행
    @POST("/api/transactions/{transactionId}/confirmation")
    suspend fun completeTransaction(
        @Path("transactionId") transactionId: Int,
        @Body request: CompleteTransactionRequest
    ): Response<CommonResponse<String>>
}
