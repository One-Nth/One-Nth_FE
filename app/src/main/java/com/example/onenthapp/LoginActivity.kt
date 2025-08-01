package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class LoginActivity : AppCompatActivity() {

    // ✅ 개발자용 계정 (하드코딩)
    private val devEmail = "1234@example.com"
    private val devPassword = "12345678a@"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEt = findViewById<EditText>(R.id.emailEditText)
        val passwordEt = findViewById<EditText>(R.id.passwordEditText)
        val loginBtn = findViewById<ImageView>(R.id.button2)

        loginBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            if (email == devEmail && password == devPassword) {
//                Toast.makeText(this, "개발자용 로그인 성공!", Toast.LENGTH_SHORT).show()

                // ✅ LoginActivity2 화면으로 이동
                val intent = Intent(this@LoginActivity, LoginActivity2::class.java)
                startActivity(intent)
                finish() // LoginActivity 종료
            } else {
                Toast.makeText(this, "아이디 또는 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

