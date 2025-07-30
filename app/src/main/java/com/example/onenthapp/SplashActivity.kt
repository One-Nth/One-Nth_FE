package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val tvLogin = findViewById<TextView>(R.id.tv_login)
        val tvSignup = findViewById<TextView>(R.id.tv_signup)

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}

