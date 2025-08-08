package com.example.onenthapp.data.userset

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface UserSetApi {

    // 1. 알림 설정 전체 조회
    @GET("user-settings")
    suspend fun getUserSettings(): Response<UserSettingsResponse>

    // 2. 스크랩 알림 on/off
    @PATCH("user-settings/scrap-alerts")
    suspend fun updateScrapAlert(@Body request: EnabledRequest): Response<AlertToggleResponse>

    // 3. 리뷰 알림 on/off
    @PATCH("user-settings/review-alerts")
    suspend fun updateReviewAlert(@Body request: EnabledRequest): Response<BaseResponse>

    // 4. 채팅 알림 설정 (전체 on/off)
    @PATCH("user-settings/chat-alerts")
    suspend fun updateChatAlert(@Body request: EnabledRequest): Response<AlertToggleResponse>

    // 5. 지역 키워드 등록
    @POST("user-settings/keyword-alters/regions/{regionId}")
    suspend fun registerRegionKeyword(@Path("regionId") regionId: Int): Response<KeywordAlertResponse>

    // 6. 지역 키워드 등록 on/off
    @PATCH("user-settings/keyword-alters/regions/{regionKeywordAlertId}")
    suspend fun toggleRegionKeyword(
        @Path("regionKeywordAlertId") regionKeywordAlertId: Int,
        @Body request: EnabledRequest
    ): Response<BaseResponse>

    // 7. 키워드 등록
    @POST("user-settings/keyword-alerts")
    suspend fun registerKeyword(@Body request: KeywordRequest): Response<BaseResponse>

    // 8. 키워드 등록 on/off
    @PATCH("user-settings/keyword-alerts/{keywordAlertId}")
    suspend fun toggleKeyword(
        @Path("keywordAlertId") keywordAlertId: Int,
        @Body request: EnabledRequest
    ): Response<BaseResponse>

    // 9. 알림 받을 키워드 삭제
    @HTTP(method = "PATCH", path = "user-settings/keyword-alters", hasBody = true)
    suspend fun deleteKeywords(@Body request: DeleteKeywordsRequest): Response<BaseResponse>

    // 10. 차단 목록 조회
    @GET("user-settings/blocks")
    suspend fun getBlockedUsers(): Response<BlockedUsersResponse>

    // 11. 차단 해제
    @DELETE("user-settings/blocks/{blockedUserId}")
    suspend fun unblockUser(@Path("blockedUserId") blockedUserId: Int): Response<BaseResponse>
}
