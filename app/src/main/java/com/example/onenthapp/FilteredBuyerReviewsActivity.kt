package com.example.onenthapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.data.MyReview
import com.example.onenthapp.util.TokenManager
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class FilteredBuyerReviewsActivity : AppCompatActivity() {

    private lateinit var adapter: BuyerFullReviewAdapter
    private val reviewApi get() = RetrofitInstance.reviewApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ✅ 기존 레이아웃 재사용
        setContentView(R.layout.activity_buyer_review)

        // Toolbar
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            // 아이템 타입 필터에 따라 타이틀 분기
            val f = intent.getStringExtra("itemTypeFilter")
            title = when (f?.uppercase()) {
                "PURCHASE" -> "구매자 거래 후기 (같이사요)"
                "SHARE"    -> "구매자 거래 후기 (함께나눠요)"
                else       -> "구매자 거래 후기"
            }
            setNavigationOnClickListener { finish() }
        }

        // 리스트
        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewAllReviews)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = BuyerFullReviewAdapter()
        rv.adapter = adapter

        // 인텐트
        val userId = intent.getLongExtra("userId", -1L)
        val itemTypeFilter = intent.getStringExtra("itemTypeFilter") // "PURCHASE" | "SHARE" | null
        if (userId <= 0) { finish(); return }

        // 로딩
        lifecycleScope.launch {
            try {
                val token = TokenManager.getAccessToken()
                val resp = reviewApi.getUserReviews(userId, "Bearer ${token ?: ""}")
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val all = resp.body()!!.result.reviewList

                    // ✅ 타입 필터 적용
                    val filtered = itemTypeFilter?.let { f ->
                        all.filter { it.itemType.equals(f, ignoreCase = true) }
                    } ?: all

                    val list = filtered.map { r ->
                        MyReview(
                            reviewId = r.reviewId,
                            itemType = r.itemType,
                            itemId = r.itemId,
                            itemTitle = r.itemTitle,
                            createdAt = r.createdAt,
                            reviewerId = r.reviewerId,
                            reviewerNickName = (r.reviewerNickName ?: "").ifBlank { "익명" },
                            reviewerProfileImageUrl = r.reviewerProfileImageUrl,
                            reviewTargetId = r.reviewTargetId,
                            content = r.content ?: "",
                            rate = r.rate,
                            reviewImageList = r.reviewImageList ?: emptyList()
                        )
                    }
                    adapter.submitList(list)
                } else {
                    adapter.submitList(emptyList())
                    Toast.makeText(this@FilteredBuyerReviewsActivity, "후기 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                adapter.submitList(emptyList())
                Toast.makeText(this@FilteredBuyerReviewsActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
