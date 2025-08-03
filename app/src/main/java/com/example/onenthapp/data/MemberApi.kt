package com.example.onenthapp.data

import NicknameResponse
import ProfileImageResponse
import ProfileResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part

interface MemberApi {
    @DELETE("members/withdraw")
    suspend fun withdraw(): Response<CommonResponse<String>>

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
}
