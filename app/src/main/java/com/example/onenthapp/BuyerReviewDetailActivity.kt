package com.example.onenthapp

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.data.MyReview
import com.example.onenthapp.databinding.ActivityBuyerReviewBinding
import com.example.onenthapp.util.TokenManager
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class BuyerReviewDetailActivity : AppCompatActivity() {
    private lateinit var adapter: BuyerFullReviewAdapter
    private val reviewApi = RetrofitInstance.reviewApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_review)

        findViewById<MaterialToolbar>(R.id.topAppBar)
            .setNavigationOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.recyclerViewAllReviews)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = BuyerFullReviewAdapter()
        rv.adapter = adapter

        val userId = intent.getLongExtra("userId", -1L)
        if (userId <= 0) { finish(); return }

        lifecycleScope.launch {
            try {
                val token = TokenManager.getAccessToken()
                val resp = reviewApi.getUserReviews(userId, "Bearer $token")
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val list = resp.body()!!.result.reviewList.map { r ->
                        MyReview(
                            reviewId = r.reviewId,
                            itemType = r.itemType,
                            itemId = r.itemId,
                            createdAt = r.createdAt,
                            reviewerId = r.reviewerId,
                            reviewerNickName = r.reviewerNickName,
                            reviewerProfileImageUrl = r.reviewerProfileImageUrl,
                            reviewTargetId = r.reviewTargetId,
                            content = r.content,
                            rate = r.rate,
                            reviewImageList = r.reviewImageList
                        )
                    }
                    adapter.submitList(list)
                } else {
                    Toast.makeText(this@BuyerReviewDetailActivity, "후기 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BuyerReviewDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
