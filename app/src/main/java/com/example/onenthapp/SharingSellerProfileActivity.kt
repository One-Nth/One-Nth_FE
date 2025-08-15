package com.example.onenthapp

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.bumptech.glide.Glide
import com.example.onenthapp.util.TokenManager
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch



class SharingSellerProfileActivity : AppCompatActivity() {

    private lateinit var buyerAdapter: BuyerReviewAdapter

    // 뷰 참조
    private val toolbar by lazy { findViewById<MaterialToolbar>(R.id.topAppBar) }
    private val ivProfile by lazy { findViewById<ImageView>(R.id.profileImage) }
    private val tvNickname by lazy { findViewById<TextView>(R.id.nickname) }

    private val rvBuyerReviews by lazy { findViewById<RecyclerView>(R.id.buyerReviewRecyclerView) }
    private val scrollBar by lazy { findViewById<View>(R.id.scrollBar) }

    private val tvSellCount by lazy { findViewById<TextView>(R.id.sellCount) }
    private val tvReviewCount by lazy { findViewById<TextView>(R.id.reviewCount) }
    private val ratingSummary by lazy { findViewById<RatingBar>(R.id.ratingSummary) }

    // API
    private val reviewApi get() = RetrofitInstance.reviewApi
    private val memberApi get() = RetrofitInstance.memberApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_profile)

        // Toolbar → 액션바
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // 인텐트로 넘어온 값(없어도 안전)
        val sellerId            = intent.getLongExtra("sellerId", -1L).takeIf { it > 0 }
        val sellerName          = intent.getStringExtra("sellerName").orEmpty()
        val sellerProfileUrl    = intent.getStringExtra("sellerProfileImageUrl").orEmpty()
        val sellerVerified      = intent.getBooleanExtra("sellerVerified", false)

        // 상단 프로필 초기 세팅 (넘어온 값 우선)
        supportActionBar?.title = if (sellerName.isNotBlank()) sellerName else "프로필"
        if (sellerName.isNotBlank()) tvNickname.text = sellerName
        if (sellerProfileUrl.isNotBlank()) {
            Glide.with(this)
                .load(sellerProfileUrl)
                .circleCrop()
                .placeholder(R.drawable.profile_base)
                .error(R.drawable.profile_base)
                .into(ivProfile)
        } else {
            ivProfile.setImageResource(R.drawable.profile_base)
        }
        // sellerVerified 문구는 XML에 id가 없어서 그대로 두었고, 필요하면 id 추가해서 바꿔도 돼.

        // 후기 리스트(가로)
        buyerAdapter = BuyerReviewAdapter()
        rvBuyerReviews.apply {
            adapter = buyerAdapter
            layoutManager = LinearLayoutManager(this@SharingSellerProfileActivity, LinearLayoutManager.HORIZONTAL, false)
        }
        attachScrollBar(rvBuyerReviews, scrollBar)

        // 데이터 로드: sellerId 있으면 타겟 유저의 데이터 로딩
        sellerId?.let {
            loadTradeSummary(it)
            loadBuyerReviews(it)
        }
    }

    // 하단 스크롤바 연동 (MyPageReviewsActivity와 동일 로직)
    private fun attachScrollBar(rv: RecyclerView, bar: View) {
        val scrollContainerWidthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 350f, resources.displayMetrics
        ).toInt()

        rv.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val offset = recyclerView.computeHorizontalScrollOffset()
                val extent = recyclerView.computeHorizontalScrollExtent()
                val range = recyclerView.computeHorizontalScrollRange()

                val proportion = offset.toFloat() / (range - extent).coerceAtLeast(1).toFloat()
                val maxScrollX = scrollContainerWidthPx - bar.width
                bar.translationX = maxScrollX * proportion
            }
        })
    }

    // 판매/리뷰 요약
    private fun loadTradeSummary(targetUserId: Long) {
        lifecycleScope.launch {
            try {
                val resp = memberApi.getUserTradeHistory(targetUserId)
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val r = resp.body()!!.result

                    tvSellCount.text = r.totalDealsCount.toString()
                    tvReviewCount.text = r.reviewCount.toString()

                    val avg = when {
                        r.reviewCount <= 0 -> 0f
                        r.totalRating > 5f -> (r.totalRating / r.reviewCount) // 합계로 내려온 경우 가정
                        else -> r.totalRating                                // 이미 평균인 경우
                    }.coerceIn(0f, 5f)

                    ratingSummary.setIsIndicator(true)
                    ratingSummary.rating = avg
                } else {
                    tvSellCount.text = "0"
                    tvReviewCount.text = "0"
                    ratingSummary.setIsIndicator(true)
                    ratingSummary.rating = 0f
                    Log.e("SellerProfile", "TradeSummary 실패: ${resp.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                tvSellCount.text = "0"
                tvReviewCount.text = "0"
                ratingSummary.setIsIndicator(true)
                ratingSummary.rating = 0f
                Log.e("SellerProfile", "TradeSummary 오류: ${e.message}")
            }
        }
    }

    // 구매자 거래 후기 리스트
    private fun loadBuyerReviews(targetUserId: Long) {
        val token = TokenManager.getAccessToken()
        lifecycleScope.launch {
            try {
                // 백엔드가 인증 헤더를 요구한다면 아래처럼 전달 (요구 안 하면 "" 전달돼도 컴파일 문제 없음)
                val resp = reviewApi.getUserReviews(targetUserId, "Bearer ${token ?: ""}")
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val reviewList = resp.body()!!.result.reviewList.map { r ->
                        // MyReview 모델과 어댑터는 MyPageReviewsActivity에서 쓰던 그대로 재사용
                        com.example.onenthapp.data.MyReview(
                            reviewId = r.reviewId,
                            itemType = r.itemType,
                            itemId = r.itemId,
                            itemTitle = r.itemTitle,
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
                    buyerAdapter.updateList(reviewList)
                } else {
                    buyerAdapter.updateList(emptyList())
                    Log.e("SellerProfile", "BuyerReviews 실패: ${resp.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                buyerAdapter.updateList(emptyList())
                Log.e("SellerProfile", "BuyerReviews 오류: ${e.message}")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish(); true
        } else super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish(); return true
    }
}
