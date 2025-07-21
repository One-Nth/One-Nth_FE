package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyReviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_review)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // 현재 Activity 종료 → 이전 Fragment 화면으로 돌아감
        }

        // ✅ RecyclerView 연결
        val recyclerView = findViewById<RecyclerView>(R.id.reviewRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // ✅ 예시 데이터
        val reviews = listOf(
            ReviewData("abced", 5, "정말 친절했어요!"),
            ReviewData("dfsfg", 4, "빠른 거래 감사합니다"),
            ReviewData("jeongmin", 3, "상품이랑 조금 달랐어요"),
        )


        // ✅ 어댑터 연결
         recyclerView.adapter = ReviewAdapter(reviews)


        // ✅ TextView 클릭 시 BuyerReview 페이지로 이동
        val titleText = findViewById<TextView>(R.id.buyerReviewTitle)
        titleText.setOnClickListener {
            val intent = Intent(this, BuyerReview::class.java)
            // 예시로 첫 번째 리뷰 데이터 전송
            intent.putExtra("name", reviews[0].name)
            intent.putExtra("rating", reviews[0].rating)
            intent.putExtra("text", reviews[0].text)
            startActivity(intent)
        }

        val editMyReviewText = findViewById<TextView>(R.id.editMyReviewText)

        editMyReviewText.setOnClickListener {
            val intent = Intent(this, EditMyReviewActivity::class.java)
            startActivity(intent)

        }

    }
}


