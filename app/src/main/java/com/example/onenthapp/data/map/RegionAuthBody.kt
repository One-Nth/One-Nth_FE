package com.example.onenthapp.data.map

// 지역 인증 요청
data class RegionAuthRequest(
    val latitude: Double,
    val longitude: Double
)

// 지역 인증 응답
data class RegionAuthResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: RegionAuthResult?
)

data class RegionAuthResult(
    val detectedRegionName: String,
    val requestedRegionName: String,
    val verified: Boolean
)
