package com.example.onenthapp.data.post

import com.example.onenthapp.data.CommonResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApi {
    @Multipart
    @POST("post/{postType}")
    suspend fun createPost(
        @Header("Authorization") bearer: String,
        @Path("postType") postType: String,       // "LIFE_TIP" | "DISCOUNT" | "RESTAURANT"
        @Part("post") postJson: RequestBody,      // JSON 문자열
        @Part images: List<MultipartBody.Part>? = null // "images" 최대 5장
    ): Response<CreatePostResponse>

    @GET("post/search")
    suspend fun searchPosts(
        @Header("Authorization") bearer: String,       // "Bearer xxx"
        @Query("postType") postType: String,          // "LIFE_TIP" | "DISCOUNT" | "RESTAURANT"
        @Query("regionName") regionName: String? = null,
        @Query("keyword") keyword: String? = null,    // "#태그" 형식도 그대로 전달
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<SearchPostResponse>

    @GET("post/{postId}")
    suspend fun getPostDetail(
        @Header("Authorization") bearer: String,
        @Path("postId") postId: Long
    ): Response<PostDetailResponse>

    @Multipart
    @PATCH("post/{postId}")
    suspend fun patchPost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,
        @Part("post") postJson: RequestBody,                 // JSON 문자열
        @Part images: List<MultipartBody.Part>? = null       // 선택
    ): BaseResponse<PatchPostResult>

    @DELETE("post/{postId}")
    suspend fun deletePost(
        @Header("Authorization") bearer: String,
        @Path("postId") postId: Long
    ): Response<CommonResponse<Unit>>   // result는 빈 객체이므로 Unit/Any 사용 가능
}
