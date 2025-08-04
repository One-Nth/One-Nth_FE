package com.example.onenthapp

import android.content.Intent
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
//                    viewModel.loginWithKakao(code) { success, message, isNew ->
//                        if (success) {
//                            val nextActivity = if (isNew) LoginActivity2::class.java else MainActivity::class.java
//                            startActivity(Intent(this, nextActivity))
//                            if (!isNew) finish()
//                        } else {
//                            Toast.makeText(this, message ?: "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
//                        }
//                    }
                }
            }
        }
    }
}

