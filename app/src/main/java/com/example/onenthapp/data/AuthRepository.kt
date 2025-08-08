package com.example.onenthapp.data

import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.data.userset.KakaoSignupRequest
import com.example.onenthapp.data.userset.KakaoSignupResult

class AuthRepository {
    private val api = RetrofitInstance.authApi

    suspend fun sendEmailCode(email: String) =
        api.requestCode(mapOf("email" to email))

    suspend fun verifyEmailCode(email: String, code: String) =
        api.verifyCode(mapOf("email" to email, "code" to code))

//    // ✅ 회원가입 호출 함수
//    suspend fun signup(request: SignupRequest) = api.signup(request)

    // ✅ LOCAL 회원가입
    suspend fun Signup(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        nickname: String,
        regionName: String,
        marketingAgree: Boolean
    ): Result<LocalSignupResult> = try {
        val resp = api.signup(
            SignupRequest(name, email, password, confirmPassword, nickname, regionName, marketingAgree)
        )
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body?.isSuccess == true && body.result != null) Result.success(body.result!!)
            else Result.failure(IllegalStateException(body?.message ?: "회원가입 실패"))
        } else {
            Result.failure(IllegalStateException("HTTP ${resp.code()} - ${resp.errorBody()?.string()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    suspend fun login(email: String, password: String) =
        api.login(LoginRequest(email, password))


    suspend fun requestPasswordResetCode(name: String, email: String) =
        api.requestPasswordResetCode(mapOf("name" to name, "email" to email))

    suspend fun verifyPasswordResetCode(email: String, code: String) =
        api.verifyPasswordResetCode(mapOf("email" to email, "code" to code))

    suspend fun resetPassword(email: String, newPassword: String) =
        api.resetPassword(mapOf("email" to email, "newPassword" to newPassword))

    suspend fun loginWithKakaoAccessToken(kakaoAccessToken: String): Result<KakaoLoginResult> = try {
        val resp = api.loginWithKakao(KakaoLoginRequest(kakaoAccessToken))
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body?.isSuccess == true && body.result != null) {
                Result.success(body.result!!)
            } else {
                Result.failure(IllegalStateException(body?.message ?: "카카오 로그인 실패"))
            }
        } else {
            Result.failure(IllegalStateException("HTTP ${resp.code()} - ${resp.errorBody()?.string()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }



    // ✅ KAKAO 회원가입
    suspend fun kakaoSignup(req: KakaoSignupRequest): Result<KakaoSignupResult> = try {
        val resp = api.signupWithKakao(req)
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body?.isSuccess == true && body.result != null) Result.success(body.result!!)
            else Result.failure(IllegalStateException(body?.message ?: "카카오 회원가입 실패"))
        } else {
            Result.failure(IllegalStateException("HTTP ${resp.code()} - ${resp.errorBody()?.string()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

}