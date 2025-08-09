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
    val emailOk = MutableLiveData<Boolean>()
    val codeStatus = MutableLiveData<String?>()  // 인증 코드 검증 상태
    val codeOk = MutableLiveData<Boolean>()
    val signupStatus = MutableLiveData<String?>() // ✅ 회원가입 결과 저장
    val signupSuccess = MutableLiveData<Boolean?>()

    fun requestCode() = viewModelScope.launch {
        val emailValue = email.value ?: ""
        if (!isValidEmail(emailValue)) {
            emailStatus.postValue("올바른 형식의 이메일을 입력해주세요.")
            emailOk.postValue(false)
            return@launch
        }

        try {
            val response = repo.sendEmailCode(emailValue)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess == true) {
                    emailStatus.postValue(body.result ?: "사용 가능한 아이디입니다.")
                    emailOk.postValue(true)
                } else {
                    emailStatus.postValue(body?.result ?: body?.message ?: "요청 실패")
                    emailOk.postValue(false)
                }

            } else {
                // ❗ 비정상 코드(500 등) → errorBody에서 result/message 추출
                val msg = extractServerMsg(response.errorBody()?.string())
                emailStatus.postValue(msg)
            }
        } catch (e: Exception) {
            Log.e("API_DEBUG", "네트워크 오류", e)
            emailStatus.postValue("네트워크 오류가 발생했습니다.")
            emailOk.postValue(false)
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
                    codeStatus.postValue(body.result ?: "인증 완료되었습니다.") // ✅ result 표시
                    codeOk.postValue(true)

                } else {
                    codeStatus.postValue(body?.result ?: body?.message ?: "인증번호를 다시 입력해주세요.")
                    codeOk.postValue(false)
                }

            } else {
//                codeStatus.postValue("서버 오류 (${response.code()})")
                val msg = extractServerMsg(response.errorBody()?.string())
                codeStatus.postValue(msg)
            }
        } catch (e: Exception) {
            codeStatus.postValue("인증번호를 다시 입력해주세요.")
            codeOk.postValue(false)
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

    private fun extractServerMsg(errorBody: String?): String {
        return try {
            val json = org.json.JSONObject(errorBody ?: "")
            // result가 문자열이면 그걸 우선 사용
            when {
                json.has("result") && json.get("result") is String ->
                    json.optString("result")
                else ->
                    json.optString("message", "요청 실패")
            }
        } catch (_: Exception) {
            "요청 실패"
        }
    }

}
