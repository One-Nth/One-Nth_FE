package com.example.onenthapp.feature.login

import LoginViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.onenthapp.MainActivity
import com.example.onenthapp.R
import com.example.onenthapp.util.TokenManager

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d("DEBUG", "LoginActivity onCreate 실행됨")

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        val emailEt = findViewById<EditText>(R.id.emailEditText)
        val passwordEt = findViewById<EditText>(R.id.passwordEditText)
        val loginBtn = findViewById<ImageView>(R.id.button2)
        val findAccountBtn = findViewById<ImageButton>(R.id.button1)
        val backBtn = findViewById<ImageButton>(R.id.backButton) // 뒤로가기 버튼

        // 🔙 뒤로가기 버튼 클릭 시
        backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        loginBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            viewModel.login(email, password) { success, message, accessToken, refreshToken, memberId ->
                if (success) {
                    TokenManager.saveAccessToken(accessToken ?: "")
                    if (!refreshToken.isNullOrEmpty()) {      // ✅ 널 체크 후 저장
                        TokenManager.saveRefreshToken(refreshToken)
                        Log.d("DEBUG", "로그인 성공, 리프레시토큰: $refreshToken")
                    }
                    TokenManager.saveMemberId(memberId?.toLong() ?: -1)
                    Log.d("DEBUG", "로그인 성공, 액세스토큰: $accessToken")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, message ?: "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }


        }

        findAccountBtn.setOnClickListener {
            startActivity(Intent(this, FindAccountActivity::class.java))
        }
    }
}