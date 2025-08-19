package com.example.onenthapp.data.map

// 지역 인증 요청
data class RegionAuthRequest(
    val latitude: Double,
    val longitude: Double
)


data class RegionAuthResult(
    val detectedRegionName: String,
    val requestedRegionName: String,
    val verified: Boolean
)
