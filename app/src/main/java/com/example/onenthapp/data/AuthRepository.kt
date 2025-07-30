package com.example.onenthapp.data

import com.example.onenthapp.RetrofitInstance

class AuthRepository {
    private val api = RetrofitInstance.authApi

    suspend fun sendEmailCode(email: String) =
        api.requestCode(mapOf("email" to email))

    suspend fun verifyEmailCode(email: String, code: String) =
        api.verifyCode(mapOf("email" to email, "code" to code))

    // ✅ 회원가입 호출 함수
    suspend fun signUp(request: SignUpRequest) =
        api.signUp(request)

}