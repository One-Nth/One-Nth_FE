package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val tvLogin = findViewById<ImageButton>(R.id.emailLoginBtn)
        val tvSignup = findViewById<ImageButton>(R.id.signupBtn)
        val tvFindAccount = findViewById<TextView>(R.id.findAccountText) // 계정찾기 텍스트뷰 추가

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        tvFindAccount.setOnClickListener {
            startActivity(Intent(this, FindAccountActivity::class.java))
        }
    }
}

