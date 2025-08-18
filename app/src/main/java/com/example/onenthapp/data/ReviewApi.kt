package com.example.onenthapp.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ReviewApi {
    @Multipart
    @POST("reviews/purchase/{purchaseItemId}")
    fun submitPurchaseReview(
        @Path("purchaseItemId") purchaseItemId: Long,
        @Part("review") review: RequestBody,
        @Part images: List<MultipartBody.Part>?
    ): Call<ReviewResponse>

    @Multipart
    @POST("reviews/sharings/{sharingItemId}")
    fun submitSharingReview(
        @Path("sharingItemId") sharingItemId: Long,
        @Part("review") review: RequestBody,
        @Part images: List<MultipartBody.Part>?
    ): Call<ReviewResponse>


    @GET("reviews/mine")
    suspend fun getMyReviews(): Response<MyReviewResponse>

    @GET("reviews/{reviewId}")
    suspend fun getReviewDetail(
        @Path("reviewId") reviewId: Long,
        @Query("itemType") itemType: String
    ): Response<ReviewDetailResponse>

    @Multipart
    @POST("reviews/{reviewId}/images")
    suspend fun uploadReviewImages(
        @Path("reviewId") reviewId: Long,
        @Query("itemType") itemType: String,
        @Part images: List<MultipartBody.Part>
    ): Response<CommonResponse<String>>

    @HTTP(method = "DELETE", path = "reviews/{reviewId}/images", hasBody = true)
    suspend fun deleteReviewImages(
        @Path("reviewId") reviewId: Long,
        @Query("itemType") itemType: String,
        @Body request: DeleteReviewImageRequest
    ): Response<CommonResponse<String>>

    @PATCH("reviews/{reviewId}")
    suspend fun updateReviewContentAndRate(
        @Path("reviewId") reviewId: Long,
        @Query("itemType") itemType: String,
        @Body request: ReviewBody
    ): Response<CommonResponse<String>>


    @GET("reviews/user/{userId}")
    suspend fun getUserReviews(
        @Path("userId") userId: Long,
        @Header("Authorization") token: String
    ): Response<UserReviewResponse>
}
