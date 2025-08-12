package com.example.onenthapp.data.nwonsaved

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NwonSavedApi {
    // 1. 내 거래내역 조회, N원 아꼈어요 조회
    @GET("deals/my-history")
    suspend fun lookNwonSaved(): Response<LookNwonSavedResponse>


    // 2. 내가 거래한 상품 목록 조회
    @GET("deals/my-history/items")
    suspend fun lookMyHistoryItem(@Query("reviewStatus") reviewStatus: String): Response<LookMyHistoryItemResponse>




}