package com.example.onenthapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch


class AllMyReviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_my_review)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerMyReviews)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val api = RetrofitInstance.reviewApi

        lifecycleScope.launch {
            try {
                val response = api.getMyReviews()

                // 💡 body() 꺼내고 null 체크
                val body = response.body()
                if (response.isSuccessful && body != null && body.isSuccess) {
                    val reviews = body.result.reviewList
                    recyclerView.adapter = MyReviewAdapter(reviews)
                } else {
                    Toast.makeText(
                        this@AllMyReviewActivity,
                        "리뷰 불러오기 실패: ${body?.message ?: "알 수 없는 오류"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AllMyReviewActivity,
                    "네트워크 오류: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

