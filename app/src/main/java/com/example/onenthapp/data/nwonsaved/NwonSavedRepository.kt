package com.example.onenthapp.data.nwonsaved

import android.util.Log
import com.example.onenthapp.RetrofitInstance

class NwonSavedRepository {

    private val api = RetrofitInstance.nwonSavedApi

    // 1. 내 거래내역 조회, N원 아꼈어요 조회
    suspend fun lookNwonSaved() = api.lookNwonSaved()

    // 2. 내가 거래한 상품 목록 조회
    suspend fun lookMyHistoryItem(reviewStatus: String) =
        api.lookMyHistoryItem(reviewStatus)}