package com.example.onenthapp.model

data class Region(
    val id: Long,
    val name: String,
    val nickname: String? = null, // 마커 별칭
    val latitude: Double,
    val longitude: Double
)