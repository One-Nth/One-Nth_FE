package com.example.onenthapp

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class AccountSettingsActivity  : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()  // 현재 액티비티 종료하고 이전으로 돌아감
        }
    }
}