package com.example.onenthapp.data

import NicknameResponse
import ProfileImageResponse
import ProfileResponse
import com.example.onenthapp.data.post.MyPostsPage
import com.example.onenthapp.data.userset.BaseResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface MemberApi {
    //✅ 탈퇴하기
    @DELETE("members/withdraw")
    suspend fun withdraw(): Response<CommonResponse<String>>

    //✅ 비밀번호 변경
    @PATCH("user-settings/profile/password")
    suspend fun changePassword(
        @Body body: Map<String, String>
    ): Response<CommonResponse<Any>>

    // ✅ 닉네임 변경
    @PATCH("user-settings/profile/nickname")
    suspend fun changeNickname(
        @Body body: Map<String, String>
    ): Response<NicknameResponse>

    // ✅ 내 프로필 조회
    @GET("user-settings/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    // ✅ 프로필 이미지 변경
    @Multipart
    @PATCH("user-settings/profile/image")
    suspend fun changeProfileImage(
        @Part image: MultipartBody.Part
    ): Response<ProfileImageResponse>

    @GET("members/mypage/posts")
    suspend fun getMyPosts(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<CommonResponse<MyPostsPage>>

    @GET("members/mypage/scraps")
    suspend fun getMyScrapPosts(
        @Query("page") page: Int,   // 1부터 시작
        @Query("size") size: Int
    ): Response<CommonResponse<MyPostsPage>>

    @GET("members/mypage/likes")
    suspend fun getMyLikedPosts(
        @Query("page") page: Int,   // 1부터 시작
        @Query("size") size: Int
    ): Response<CommonResponse<MyPostsPage>>

    @GET("users/trade-history/{userId}")
    suspend fun getUserTradeHistory(
        @Path("userId") userId: Long
    ): Response<TradeHistoryResponse>

    @GET("members/mypage/items")
    suspend fun getMyItems(
        @Header("Authorization") bearerToken: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): MyPageItemsResponse

    @GET("members/mypage/scrapped-items")
    suspend fun getScrappedItems(
        @Header("Authorization") bearerToken: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): MyPageItemsResponse

    // 같이사요 삭제
    @DELETE("group-purchases/{groupPurchaseId}")
    suspend fun deleteGroupPurchase(
        @Header("Authorization") bearerToken: String,
        @Path("groupPurchaseId") id: Long
    ): BaseResponse

    // 함께나눠요 삭제
    @DELETE("sharing-items/{sharingItemId}")
    suspend fun deleteSharingItem(
        @Header("Authorization") bearerToken: String,
        @Path("sharingItemId") id: Long
    ): BaseResponse

    @DELETE("members/mypage/likes/{postId}")
    suspend fun cancelMyLikedPost(
        @Header("Authorization") bearer: String,
        @Path("postId") postId: Long
    ): Response<CommonResponse<LikeActionResult>>
}
