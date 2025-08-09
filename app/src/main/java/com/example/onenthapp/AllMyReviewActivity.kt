package com.example.onenthapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch


class AllMyReviewActivity : AppCompatActivity() {

    private lateinit var adapter: MyReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_my_review)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerMyReviews)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 1) 어댑터 먼저 붙이기 (빈 리스트)
        adapter = MyReviewAdapter(emptyList())
        recyclerView.adapter = adapter

        // 2) 캐시 닉네임으로 먼저 표시(있으면)
        adapter.setProfileData(TokenManager.getNickname(), null)

        // 3) 프로필 최신값 불러와서 주입
        loadMyProfileForAdapter()

        // 4) 내 리뷰 목록 불러오기
        loadMyReviews()
    }

    private fun loadMyProfileForAdapter() {
        val token = TokenManager.getAccessToken()
        if (token.isNullOrEmpty()) return

        lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.memberApi.getProfile()
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val result = resp.body()!!.result
                    adapter.setProfileData(result.nickname, result.profileImageUrl)
                    // (옵션) 닉네임 캐시 갱신
                    result.nickname?.let { TokenManager.saveNickname(it) }
                }
            } catch (_: Exception) {
                // 실패 시 캐시 닉네임으로만 표시
            }
        }
    }

    private fun loadMyReviews() {
        val api = RetrofitInstance.reviewApi
        lifecycleScope.launch {
            try {
                val response = api.getMyReviews()
                val body = response.body()
                if (response.isSuccessful && body != null && body.isSuccess) {
                    val reviews = body.result.reviewList
                    adapter.updateList(reviews)   // ✅ 리스트 갱신
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
