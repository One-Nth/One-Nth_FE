package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.onenthapp.data.login.AuthRepository
import com.example.onenthapp.data.login.ReissueResponse
import com.example.onenthapp.feature.login.LoginActivity
import com.example.onenthapp.feature.signup.SignupActivity
import com.example.onenthapp.feature.signup.SignupActivity2
import com.example.onenthapp.model.KakaoLoginModelFactory
import com.example.onenthapp.model.KakaoViewModel
import com.example.onenthapp.util.TokenManager
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {

    private lateinit var viewModel: KakaoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val repository = AuthRepository()
        val factory = KakaoLoginModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[KakaoViewModel::class.java]

        val emailLoginBtn = findViewById<ImageButton>(R.id.emailLoginBtn)
        val signupBtn = findViewById<ImageButton>(R.id.signupBtn)
        val kakaoLoginBtn = findViewById<ImageButton>(R.id.kakaoLoginBtn)
        val findAccountText = findViewById<TextView>(R.id.findAccountText)

        emailLoginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        signupBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        findAccountText.setOnClickListener {
            startActivity(Intent(this, FindAccountActivity::class.java))
        }

        kakaoLoginBtn.setOnClickListener {
            loginWithKakao()
        }
        tryAutoLogin()
    }

    private fun tryAutoLogin() {
        val refreshToken = TokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) return

        val api = RetrofitInstance.authApi
        val request = mapOf("refreshToken" to refreshToken)

        api.reissueToken(request).enqueue(object : Callback<ReissueResponse> {
            override fun onResponse(call: Call<ReissueResponse>, response: Response<ReissueResponse>) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val newAccessToken = response.body()!!.result.accessToken
                    TokenManager.saveAccessToken(newAccessToken)   // ✅ accessToken만 저장
                    // ❌ TokenManager.saveRefreshToken(...) 호출 금지 (reissue 응답에 없음)

                    parseUserIdFromJwt(newAccessToken)?.let { TokenManager.saveMemberId(it) }

                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                } else {
                    // 자동로그인 실패 → 로그인 화면 유지
                }
            }
            override fun onFailure(call: Call<ReissueResponse>, t: Throwable) { /* log */ }
        })
    }

    private fun loginWithKakao() {
        // 인가코드 X, 바로 accessToken 받는 방식
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
            when {
                error != null -> {
                    Log.e("KAKAO_LOGIN", "카카오 로그인 실패", error)
                    Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
                }
                token != null -> {
                    val kakaoAccessToken = token.accessToken
                    Log.d("KAKAO_LOGIN", "kakao accessToken: $kakaoAccessToken")

                    viewModel.loginWithKakaoAccessToken(kakaoAccessToken) { success, message, isNew, serverAccess, serverRefresh ->
                        if (!success) {
                            Log.w("KAKAO_LOGIN", "백엔드 로그인 실패: $message")
                            Toast.makeText(this, message ?: "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
                            return@loginWithKakaoAccessToken
                        }

                        // 서버 응답 전체 로그
                        Log.d("KAKAO_LOGIN", "카카오 로그인 성공 응답: ${viewModel.lastKakaoResult}")

                        // 서버 토큰(기존회원인 경우)에 대한 로컬 저장은 ViewModel에서 이미 처리
                        // 안전하게 재확인 저장도 가능
                        serverAccess?.let {
                            TokenManager.saveAccessToken(it)
                            parseUserIdFromJwt(it)?.let { id -> TokenManager.saveMemberId(id) }}
                        serverRefresh?.let { if (it.isNotEmpty()) TokenManager.saveRefreshToken(it) }

                        if (isNew) {
                            val r = viewModel.lastKakaoResult
                            startActivity(Intent(this, SignupActivity2::class.java).apply {
                                putExtra("loginType", "KAKAO")           // ✅ 분기 키
                                putExtra("email",     r?.email ?: "")    // ✅ 키 이름: email (was prefillEmail)
                                putExtra("serialId",  r?.serialId ?: "") // ✅ socialId로 사용
                                putExtra("prefillName", r?.name ?: "")   // 선택
                            })
                            // finish()는 회원가입 완료 후에
                        } else {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }

                    }
                }
            }
        }
    }
    private fun parseUserIdFromJwt(jwt: String): Long? {
        return try {
            val payload = jwt.split(".").getOrNull(1) ?: return null
            val decoded = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
            val json = org.json.JSONObject(String(decoded))
            json.optString("sub").toLongOrNull()
        } catch (_: Exception) {
            null
        }
    }


}

