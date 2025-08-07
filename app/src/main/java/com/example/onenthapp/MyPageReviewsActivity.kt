package com.example.onenthapp

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.data.MyReview
import com.example.onenthapp.databinding.ActivityMypageReviewsBinding
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch

class MyPageReviewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMypageReviewsBinding
    private lateinit var adapter: BuyerReviewAdapter
    private val reviewApi = RetrofitInstance.reviewApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypageReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = BuyerReviewAdapter()
        binding.buyerReviewRecyclerView.adapter = adapter
        binding.buyerReviewRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // ✅ 스크롤바 연동
        val scrollContainerWidthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            350f,
            resources.displayMetrics
        ).toInt()
        val scrollBarView = binding.scrollBar

        binding.buyerReviewRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)

                val offset = rv.computeHorizontalScrollOffset()
                val extent = rv.computeHorizontalScrollExtent()
                val range = rv.computeHorizontalScrollRange()

                val proportion = offset.toFloat() / (range - extent).coerceAtLeast(1).toFloat()
                val maxScrollX = scrollContainerWidthPx - scrollBarView.width

                scrollBarView.translationX = maxScrollX * proportion
            }
        })

        // 뒤로가기
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        loadBuyerReviews()
    }

    private fun loadBuyerReviews() {
        val userId = TokenManager.getMemberId()
        val token = TokenManager.getToken()

        if (userId == null || token.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val response = reviewApi.getUserReviews(userId, "Bearer $token")
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val reviewList = response.body()!!.result.reviewList.map { review ->
                        MyReview(
                            reviewId = review.reviewId,
                            itemType = review.itemType,
                            itemId = review.itemId,
                            createdAt = review.createdAt,
                            reviewerId = review.reviewerId,
                            reviewTargetId = review.reviewTargetId,
                            content = review.content,
                            rate = review.rate,
                            reviewImageList = review.reviewImageList
                        )
                    }
                    adapter.updateList(reviewList) // ✅ 여기서 리스트 업데이트!
                } else {
                    Toast.makeText(this@MyPageReviewsActivity, "리뷰 목록을 불러오지 못했습니다", Toast.LENGTH_SHORT).show()
                    Log.e("MyPageReviews", "응답 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Toast.makeText(this@MyPageReviewsActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                Log.e("MyPageReviews", "네트워크 오류: ${e.message}")
            }
        }
    }
}
