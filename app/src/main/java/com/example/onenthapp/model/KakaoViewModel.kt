package com.example.onenthapp.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onenthapp.data.AuthRepository
import com.example.onenthapp.data.KakaoLoginResult
import com.example.onenthapp.data.userset.KakaoSignupRequest
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch

class KakaoViewModel(private val repo: AuthRepository) : ViewModel() {

    val signupStatus = MutableLiveData<String?>()
    val signupSuccess = MutableLiveData<Boolean?>()

    // 마지막 결과를 가입 프리필에 쓰고 싶으면 보관
    var lastKakaoResult: KakaoLoginResult? = null
        private set

    fun loginWithKakaoAccessToken(
        kakaoAccessToken: String,
        onResult: (success: Boolean, message: String?, isNew: Boolean, accessToken: String?, refreshToken: String?) -> Unit
    ) = viewModelScope.launch {
        val r = repo.loginWithKakaoAccessToken(kakaoAccessToken)
        if (r.isSuccess) {
            val data = r.getOrNull()!!
            lastKakaoResult = data

            // 기존회원이면 서버가 자체 JWT(access_token/refresh_token)를 내려줌
            data.accessToken?.let { TokenManager.saveAccessToken(it) }
            data.refreshToken?.let { if (it.isNotEmpty()) TokenManager.saveRefreshToken(it) }

            onResult(true, null, data.isNew, data.accessToken, data.refreshToken)
        } else {
            onResult(false, r.exceptionOrNull()?.message ?: "카카오 로그인 실패", false, null, null)
        }
    }



    // ✅ 카카오 신규가입
    fun kakaoSignup(
        email: String,
        socialId: String,
        name: String,
        nickname: String,
        regionName: String,
        marketingAgree: Boolean
    ) = viewModelScope.launch {
        try {
            val req = KakaoSignupRequest(
                email = email,
                socialId = socialId, // ← 카카오 로그인 응답의 serialId를 그대로
                name = sanitize(name),
                nickname = sanitize(nickname),
                regionName = regionName,
                marketingAgree = marketingAgree
            )
            val r = repo.kakaoSignup(req)
            if (r.isSuccess) {
                val data = r.getOrNull()!!
                data.accessToken?.let { TokenManager.saveAccessToken(it) }
                data.refreshToken?.let { if (it.isNotEmpty()) TokenManager.saveRefreshToken(it) }
                signupStatus.postValue("카카오 회원가입 성공")
                signupSuccess.postValue(true)
            } else {
                signupStatus.postValue(r.exceptionOrNull()?.message ?: "카카오 회원가입 실패")
                signupSuccess.postValue(false)
            }
        } catch (t: Throwable) {
            signupStatus.postValue("오류: ${t.message}")
            signupSuccess.postValue(false)
        }
    }

    // DB 컬럼 초과/이모지 방지
    private fun sanitize(raw: String, maxCodePoints: Int = 20): String {
        val noEmoji = raw.replace(Regex("[^\\p{L}\\p{N}\\p{Zs}_\\-\\.]+"), "")
        val it = noEmoji.codePoints().iterator()
        val sb = StringBuilder()
        var cnt = 0
        while (it.hasNext() && cnt < maxCodePoints) {
            sb.appendCodePoint(it.nextInt()); cnt++
        }
        return sb.toString().trim()
    }
}


