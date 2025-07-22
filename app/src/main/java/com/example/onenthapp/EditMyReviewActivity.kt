package com.example.onenthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EditMyReviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_my_review) // ✅ 이건 반드시 독립된 레이아웃

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // 현재 Activity 종료 → 이전 Fragment 화면으로 돌아감
        }

        // 예시로 빈 EditText 등 입력 UI만 둠
        val nicknameView = findViewById<TextView>(R.id.reviewerNameDetail2)
        nicknameView.text = "닉네임" // 하드코딩 또는 빈 값

        val ratingView = findViewById<TextView>(R.id.starRatingDetail2)
        ratingView.text = "★★★★★"

        val reviewTextView = findViewById<TextView>(R.id.reviewTextDetail2)
        reviewTextView.text = "어쩌고저쩌고" // 빈 텍스트로 시작
    }
}

