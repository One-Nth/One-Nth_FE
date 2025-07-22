package com.example.onenthapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BuyerReview : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_review)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // 현재 Activity 종료 → 이전 Fragment 화면으로 돌아감
        }

        val name = intent.getStringExtra("name")
        val rating = intent.getIntExtra("rating", 5)
        val text = intent.getStringExtra("text")

        // 필요한 뷰에 값 설정
        findViewById<TextView>(R.id.reviewerNameDetail).text = name
        findViewById<TextView>(R.id.starRatingDetail).text = "★".repeat(rating)
        findViewById<TextView>(R.id.reviewTextDetail).text = text
    }
}

