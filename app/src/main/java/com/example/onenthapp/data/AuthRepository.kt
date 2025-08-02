package com.example.onenthapp.data

import com.example.onenthapp.RetrofitInstance

class AuthRepository {
    private val api = RetrofitInstance.authApi

    suspend fun sendEmailCode(email: String) =
        api.requestCode(mapOf("email" to email))

    suspend fun verifyEmailCode(email: String, code: String) =
        api.verifyCode(mapOf("email" to email, "code" to code))

    // ✅ 회원가입 호출 함수
    suspend fun signup(request: SignupRequest) = api.signup(request)

    suspend fun login(email: String, password: String) =
        api.login(LoginRequest(email, password))


    suspend fun requestPasswordResetCode(name: String, email: String) =
        api.requestPasswordResetCode(mapOf("name" to name, "email" to email))

    suspend fun verifyPasswordResetCode(email: String, code: String) =
        api.verifyPasswordResetCode(mapOf("email" to email, "code" to code))

    suspend fun resetPassword(email: String, newPassword: String) =
        api.resetPassword(mapOf("email" to email, "newPassword" to newPassword))

}