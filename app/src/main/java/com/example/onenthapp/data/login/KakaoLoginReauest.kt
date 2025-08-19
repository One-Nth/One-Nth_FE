package com.example.onenthapp.data.login

import com.google.gson.annotations.SerializedName

// 카카오 로그인 요청 바디 (프론트 → 서버)
data class KakaoLoginRequest(
    val accessToken: String
)


// 서버 응답 payload (서버 → 프론트)
// 백이 snake_case를 쓰므로 @SerializedName으로 매핑
data class KakaoLoginResult(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    val email: String?,
    val name: String?,
    @SerializedName("serialId") val serialId: String?,
    val isNew: Boolean
)

