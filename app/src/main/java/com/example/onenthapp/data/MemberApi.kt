package com.example.onenthapp.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH

interface MemberApi {
    @DELETE("members/withdraw")
    suspend fun withdraw(): Response<CommonResponse<String>>

    @PATCH("user-settings/profile/password")
    suspend fun changePassword(
        @Body body: Map<String, String>
    ): Response<CommonResponse<Any>>

    @PATCH("user-settings/profile/nickname")
    suspend fun changeNickname(
        @Body body: Map<String, String>
    ): Response<NicknameResponse>
}
