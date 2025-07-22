package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // 현재 Activity 종료 → 이전 Fragment 화면으로 돌아감
        }

        val alertSettings = findViewById<TextView>(R.id.notificationSettings)
        alertSettings.setOnClickListener {
            val intent = Intent(this, AlertSettingsActivity::class.java)
            startActivity(intent)
        }

    }
}

