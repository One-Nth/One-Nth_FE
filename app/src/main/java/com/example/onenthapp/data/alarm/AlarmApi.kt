package com.example.onenthapp.data.alarm

import com.example.onenthapp.data.alarm.AlarmDealResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface AlarmApi {

    @GET("alert/post")
    suspend fun getPostAlarms(): Response<AlarmPostResponse>

    @GET("alert/deal")
    suspend fun getDealAlarms(): Response<AlarmDealResponse>

    @POST("fcm/token")
    suspend fun registerFcmToken(
        @Body request: FcmTokenRequest
    )
    : Response<FcmTokenResponse>

    @DELETE("fcm/token")
    suspend fun deleteFcmToken(
        @Body request: FcmTokenRequest
    ): Response<FcmTokenResponse>

    @POST("fcm/test")
    suspend fun sendTestPush(): Response<FcmTokenResponse>
}
