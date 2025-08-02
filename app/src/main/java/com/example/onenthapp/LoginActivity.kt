package com.example.onenthapp

import LoginViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider


class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d("DEBUG", "LoginActivity onCreate 실행됨")

        // ✅ ViewModel 초기화
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        val emailEt = findViewById<EditText>(R.id.emailEditText)
        val passwordEt = findViewById<EditText>(R.id.passwordEditText)
        val loginBtn = findViewById<ImageView>(R.id.button2)
        val findAccountBtn = findViewById<ImageButton>(R.id.button1)

        loginBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ ViewModel을 통해 로그인 요청
            viewModel.login(email, password, onResult = { success: Boolean, message: String? ->
                if (success) {
//                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, message ?: "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            })

        }

        // ✅ 계정찾기 버튼 클릭 → FindAccountActivity로 이동
        findAccountBtn.setOnClickListener {
            val intent = Intent(this, FindAccountActivity::class.java)
            startActivity(intent)
        }
    }


}


