package com.example.onenthapp.data

import com.example.onenthapp.data.userset.KakaoSignupRequest
import com.example.onenthapp.data.userset.KakaoSignupResult
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("email-auth/request-code")
    suspend fun requestCode(
        @Body body: Map<String, String>
    ): Response<CommonResponse<String>>


    @POST("email-auth/verify-code")
    suspend fun verifyCode(
        @Body body: Map<String, String>
    ): Response<CommonResponse<String>>

    // ✅ LOCAL 회원가입
    @POST("members/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<CommonResponse<LocalSignupResult>>
    @POST("members/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("email-auth/password/request-code")
    suspend fun requestPasswordResetCode(
        @Body body: Map<String, String>
    ): Response<CommonResponse<String>>

    @POST("email-auth/password/verify-code")
    suspend fun verifyPasswordResetCode(
        @Body body: Map<String, String>
    ): Response<CommonResponse<String>>

    @POST("members/password/reset")
    suspend fun resetPassword(
        @Body body: Map<String, String>
    ): Response<CommonResponse<Any>>

    @POST("auth/kakao/login")
    suspend fun loginWithKakao(@Body req: KakaoLoginRequest): Response<CommonResponse<KakaoLoginResult>>

    @POST("auth/kakao/signup")
    suspend fun signupWithKakao(
        @Body req: KakaoSignupRequest
    ): Response<CommonResponse<KakaoSignupResult>>

    @POST("members/reissue")
    fun reissueToken(@Body request: Map<String, String>): Call<ReissueResponse>
}
