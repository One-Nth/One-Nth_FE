package com.example.onenthapp.data.userset

import com.google.gson.annotations.SerializedName

// 요청 DTO
data class KakaoSignupRequest(
    val email: String,        // 카카오에서 받음
    val socialId: String,     // 카카오 serialId → 요청의 socialId
    val name: String,         // 유저 입력
    val nickname: String,     // 카카오 닉네임 프리필 + 수정 가능
    val regionName: String,   // 유저 입력
    val marketingAgree: Boolean // 유저 입력
)

// 응답 DTO (login과 동일 구조)
data class KakaoSignupResult(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    val email: String?,
    val name: String?,
    @SerializedName("serialId") val serialId: String?,
    val isNew: Boolean
)

