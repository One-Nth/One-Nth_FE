package com.example.onenthapp

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LikePostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like_post)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewLikePost)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // ✅ 뒤로가기 버튼 동작 추가
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // 현재 Activity 종료 → 이전 화면으로 이동
        }

        // 기존 Adapter 재사용
        val posts = listOf(
            mapOf(
                "category" to "생활 정보",
                "title" to "OOO카페",
                "content" to "아메리카노 2000원 행사해요~",
                "commentCount" to "2",
                "likeCount" to "1",
                "views" to "24",
                "time" to "3분 전"
            ),
            mapOf(
                "category" to "우리 동네 맛집/카페",
                "title" to "OOO카페",
                "content" to "아메리카노 2000원 행사해요~",
                "commentCount" to "1",
                "likeCount" to "5",
                "views" to "11",
                "time" to "10분 전"
            )
        )

        recyclerView.adapter = ScrapPostAdapter(posts) // 📌 스크랩 게시글 Adapter 그대로 사용
    }
}
