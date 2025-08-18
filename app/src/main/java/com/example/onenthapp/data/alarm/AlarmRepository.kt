package com.example.onenthapp.data.alarm

import com.example.onenthapp.RetrofitInstance
import retrofit2.Response

class AlarmRepository {

    private val alarmApi = RetrofitInstance.alarmApi

    suspend fun getPostAlarms(): Response<AlarmPostResponse> {
        return alarmApi.getPostAlarms()
    }

    suspend fun getDealAlarms(): Response<AlarmDealResponse> {
        return alarmApi.getDealAlarms()
    }

    suspend fun registerFcmToken(fcmToken: String): Response<FcmTokenResponse> {
        val request = FcmTokenRequest(fcmToken)
        return alarmApi.registerFcmToken(request)
    }

    suspend fun deleteFcmToken(fcmToken: String): Response<FcmTokenResponse> {
        val request = FcmTokenRequest(fcmToken)
        return alarmApi.deleteFcmToken(request)
    }

    suspend fun sendTestPush(): Response<FcmTokenResponse> {
        return alarmApi.sendTestPush()
    }
}