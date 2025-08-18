package com.example.onenthapp.data.transaction

import com.example.onenthapp.data.CommonResponse
import retrofit2.Response
import retrofit2.http.*

interface TransactionApi {

    // 1. 거래 확정 폼 발행 -
    @POST("deals/confirmation")
    suspend fun confirmationTransaction(
        @Query("roomName") roomName: String,
        @Body request: DealCompletionRequest
    ): Response<CommonResponse<String>>

    // 2. 거래 완료 폼 발행
    @POST("deals/completion")
    suspend fun completeTransaction(
        @Query("roomName") roomName: String,
        @Body request: CompleteTransactionRequest
    ): Response<CommonResponse<String>>

    // 3. 내가 거래한 상품 목록 조회
    @GET("deals/my-history/items")
    suspend fun getMyHistory(
        @Query("reviewStatus") reviewStatus: String
    ): Response<AvailableProductsResponse>

    // 4. 거래 확정 폼 조회
    @GET("deals/confirmation/{roomName}")
    suspend fun getDealConfirmationForm(
        @Path("roomName") roomName: String
    ): Response<DealConfirmationResponse>

    // 5. 거래 가능한 상품 조회
    @GET("deals/available-products")
    suspend fun getAvailableProducts(): Response<AvailableProductsResponse>

    // 6. 거래 취소
    @HTTP(method = "DELETE", path = "deals/cancellation", hasBody = true)
    suspend fun cancelTransaction(
        @Query("roomName") roomName: String,
        @Body request: CancelTransactionRequest
    ): Response<CommonResponse<String>>

}
