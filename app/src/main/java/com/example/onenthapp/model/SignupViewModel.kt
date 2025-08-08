package com.example.onenthapp.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onenthapp.data.AuthRepository
import com.example.onenthapp.data.SignupRequest
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch

class SignupViewModel(private val repo: AuthRepository) : ViewModel() {
    val email = MutableLiveData<String>()
    val emailStatus = MutableLiveData<String?>() // 이메일 발송 상태
    val codeStatus = MutableLiveData<String?>()  // 인증 코드 검증 상태
    val signupStatus = MutableLiveData<String?>() // ✅ 회원가입 결과 저장
    val signupSuccess = MutableLiveData<Boolean?>()

    fun requestCode() = viewModelScope.launch {
        val emailValue = email.value ?: ""
        if (!isValidEmail(emailValue)) {
            emailStatus.postValue("올바른 형식의 이메일을 입력해주세요.")
            return@launch
        }

        try {
            val response = repo.sendEmailCode(emailValue)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess == true) {
                    emailStatus.postValue("사용 가능한 아이디입니다.") // ✅ 여기서 메시지 변경
                } else {
                    emailStatus.postValue(body?.message ?: "요청 실패")
                }

            } else {
                emailStatus.postValue("API 요청 실패 (${response.code()})")
            }
        } catch (e: Exception) {
            Log.e("API_DEBUG", "네트워크 오류", e)
            emailStatus.postValue("네트워크 오류가 발생했습니다.")
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return email.matches(regex)
    }

    fun verifyCode(code: String) = viewModelScope.launch {
        val emailValue = email.value ?: return@launch
        try {
            val response = repo.verifyEmailCode(emailValue, code)
            Log.e("API_DEBUG", "응답 코드: ${response.code()}")
            Log.e("API_DEBUG", "응답 바디: ${response.body()}")
            Log.e("API_DEBUG", "에러 바디: ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess == true) {
                    codeStatus.postValue("인증 완료되었습니다.") // ✅ 메시지 고정
                } else {
                    codeStatus.postValue(body?.message ?: "인증번호를 다시 입력해주세요.")
                }

            } else {
                codeStatus.postValue("서버 오류 (${response.code()})")
            }
        } catch (e: Exception) {
            codeStatus.postValue("네트워크 오류가 발생했습니다.")
        }
    }

    private fun isValidPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{10,}$")
        return regex.matches(password)
    }

    // ✅ 일반 회원가입
    fun localSignup(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        nickname: String,
        regionName: String,
        marketingAgree: Boolean
    ) = viewModelScope.launch {
        try {
            val r = repo.Signup(
                name = sanitize(name),
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                nickname = sanitize(nickname),
                regionName = regionName,
                marketingAgree = marketingAgree
            )
            if (r.isSuccess) {
                val data = r.getOrNull()!!
                data.accessToken?.let { TokenManager.saveToken(it) }
                data.refreshToken?.let { if (it.isNotEmpty()) TokenManager.saveRefreshToken(it) }
                signupStatus.postValue("회원가입 성공")
                signupSuccess.postValue(true)
            } else {
                signupStatus.postValue(r.exceptionOrNull()?.message ?: "회원가입 실패")
                signupSuccess.postValue(false)
            }
        } catch (t: Throwable) {
            signupStatus.postValue("오류: ${t.message}")
            signupSuccess.postValue(false)
        }
    }

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
