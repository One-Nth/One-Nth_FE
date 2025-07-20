package com.example.onenthapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BuyerReview : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_review)

        val name = intent.getStringExtra("name")
        val rating = intent.getIntExtra("rating", 5)
        val text = intent.getStringExtra("text")

        // 필요한 뷰에 값 설정
        findViewById<TextView>(R.id.reviewerNameDetail).text = name
        findViewById<TextView>(R.id.starRatingDetail).text = "★".repeat(rating)
        findViewById<TextView>(R.id.reviewTextDetail).text = text
    }
}

