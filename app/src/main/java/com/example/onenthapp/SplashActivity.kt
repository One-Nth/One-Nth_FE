package com.example.onenthapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.onenthapp.data.AuthRepository
import com.example.onenthapp.data.KakaoSignupRequest
import com.example.onenthapp.model.KakaoLoginModelFactory
import com.example.onenthapp.model.KakaoViewModel
import com.kakao.sdk.auth.AuthCodeClient

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
    }

    private fun loginWithKakao() {

        AuthCodeClient.instance.authorizeWithKakaoAccount(this) { code, error ->
            when {
                error != null -> {
                    Log.e("KAKAO_LOGIN", "카카오 로그인 실패", error)
                    Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
                }
                code != null -> {
                    Log.d("KAKAO_LOGIN", "인가 코드 발급: $code")
                    viewModel.loginWithKakao(code) { success, message, isNew ->
                        if (success) {
                            val nextActivity = if (isNew) LoginActivity2::class.java else MainActivity::class.java
                            startActivity(Intent(this, nextActivity))
                            if (!isNew) finish()
                        } else {
                            Toast.makeText(this, message ?: "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

//    private fun loginWithKakaoWebOAuth() {
//        val clientId = "b383420f841303eead9d711d8ab147d2" // 카카오 REST API 키
//        //val redirectUri = "http://10.0.2.2:8080/api/auth/kakao/login" // 백이 요구하는 redirect_uri
////        val redirectUri = "http://localhost:3000/api/auth/kakao/callback"
//        val redirectUri = "b915f02e5deddf0811e43b08afbdbe41://oauth"
//
////        val redirectUri = "http://localhost:8080/api/auth/kakao/callback"
//        val authUrl = "https://kauth.kakao.com/oauth/authorize" +
//                "?client_id=$clientId" +
//                "&redirect_uri=$redirectUri" +
//                "&response_type=code"
//
//        Log.d("KAKAO_LOGIN", "카카오 로그인 URL 호출: $authUrl")
//
//        try {
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
//            startActivity(intent)
//            Toast.makeText(this, "카카오 로그인 페이지로 이동합니다.", Toast.LENGTH_SHORT).show()
//        } catch (e: Exception) {
//            Log.e("KAKAO_LOGIN", "카카오 로그인 페이지 열기 실패", e)
//            Toast.makeText(this, "카카오 로그인 실행 실패: ${e.message}", Toast.LENGTH_LONG).show()
//        }
//    }
//
//
//
//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        handleKakaoIntent(intent)
//    }
//
//    private fun handleKakaoIntent(intent: Intent?) {
//        intent?.data?.let { uri ->
//            Log.d("KAKAO_LOGIN", "딥링크 도착: $uri") // 이 로그부터 반드시 찍혀야 함
//
//            if (uri.toString().startsWith("b915f02e5deddf0811e43b08afbdbe41://oauth")) {
//                val code = uri.getQueryParameter("code")
//                Log.d("KAKAO_LOGIN", "인가코드 수신: $code")
//                if (code != null) {
//                    viewModel.loginWithKakao(code) { success, message, isNew ->
//                        runOnUiThread {
//                            if (success) {
//                                val next = if (isNew) SignupActivity::class.java else MainActivity::class.java
//                                startActivity(Intent(this, next))
//                                finish()
//                            } else {
//                                Toast.makeText(this, message ?: "로그인 실패", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }



}

