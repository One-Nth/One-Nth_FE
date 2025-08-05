package com.example.onenthapp.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ReviewApi {

    @Multipart
    @POST("/api/reviews/purchase/{purchaseItemId}")
    fun submitPurchaseReview(
        @Path("purchaseItemId") purchaseItemId: Long,
        @Part("review") review: RequestBody,
        @Part images: List<MultipartBody.Part>? = null
    ): Call<ReviewResponse>
}
