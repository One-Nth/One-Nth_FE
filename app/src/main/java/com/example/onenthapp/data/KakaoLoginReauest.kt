package com.example.onenthapp.data

data class KakaoLoginRequest(
    val code: String
)

data class KakaoLoginResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: KakaoLoginResult
)

data class KakaoLoginResult(
    val access_token: String,
    val email: String,
    val name: String,
    val serialId: String,
    val isNew: Boolean
)

data class KakaoSignupRequest(
    val email: String,
    val socialId: String,
    val name: String,
    val nickname: String,
    val regionName: String,
    val marketingAgree: Boolean
)

