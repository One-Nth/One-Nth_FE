package com.example.onenthapp.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PlusApi {
    /** 같이 사요 */
    @Multipart
    @POST("group-purchases")
    suspend fun createGroupPurchase(
        @Part("data") data: RequestBody,
        @Part imageFiles: List<MultipartBody.Part>
    ): Response<BuyResponse>

    /** 함께 나눠요 */
    @Multipart
    @POST("sharing-items")
    suspend fun createSharingItem(
        @Part("data") data: RequestBody,
        @Part imageFiles: List<MultipartBody.Part>
    ): Response<ShareResponse>
}
