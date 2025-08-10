package com.example.onenthapp.data.post

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PostApi {
    @Multipart
    @POST("post/{postType}")
    suspend fun createPost(
        @Header("Authorization") bearer: String,
        @Path("postType") postType: String,       // "LIFE_TIP" | "DISCOUNT" | "RESTAURANT"
        @Part("post") postJson: RequestBody,      // JSON 문자열
        @Part images: List<MultipartBody.Part>? = null // "images" 최대 5장
    ): Response<CreatePostResponse>
}
