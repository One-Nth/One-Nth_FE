package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.data.MyReview
import com.example.onenthapp.databinding.ActivityMypageReviewsBinding
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MyPageReviewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMypageReviewsBinding
    private lateinit var adapter: BuyerReviewAdapter
    private val reviewApi = RetrofitInstance.reviewApi
    private lateinit var myEditAdapter: MyOwnReviewEditAdapter

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

        binding.btnGoAllReviews.setOnClickListener {
            val userId = TokenManager.getMemberId() ?: return@setOnClickListener
            startActivity(Intent(this, BuyerReviewDetailActivity::class.java).apply {
                putExtra("userId", userId)
            })
        }


        // 뒤로가기
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        // ✅ 내가 쓴 후기 수정하기 리스트 (세로)
        myEditAdapter = MyOwnReviewEditAdapter { review ->
            // 편집 화면으로 이동
            startActivity(Intent(this, ReviewEditActivity::class.java).apply {
                putExtra("reviewId", review.reviewId)
                putExtra("itemType", review.itemType)   // "PURCHASE" | "SHARE"
                putExtra("canEdit", true)
                // 보기 전용일 때 보여줄 닉/프로필 넘기고 싶으면 여기에서 세팅
            })
        }
        binding.editReviewRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.editReviewRecyclerView.adapter = myEditAdapter
        binding.editReviewRecyclerView.isNestedScrollingEnabled = false

        loadBuyerReviews()
        bindProfile()
        loadTradeSummary()
        loadMyWrittenReviews()
    }

    private fun loadMyWrittenReviews() {
        lifecycleScope.launch {
            try {
                val resp = reviewApi.getMyReviews()
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    // 서버 모델과 앱 모델이 동일 이름이면 그대로 매핑
                    val list = resp.body()!!.result.reviewList.map { r ->
                        MyReview(
                            reviewId = r.reviewId,
                            itemType = r.itemType,
                            itemId = r.itemId,
                            itemTitle = r.itemTitle,                 // ✅ 제목 사용
                            createdAt = r.createdAt,
                            reviewerId = r.reviewerId,
                            reviewerNickName = r.reviewerNickName,
                            reviewerProfileImageUrl = r.reviewerProfileImageUrl,
                            reviewTargetId = r.reviewTargetId,
                            content = r.content,
                            rate = r.rate,
                            reviewImageList = r.reviewImageList      // ✅ 썸네일 대용
                        )
                    }
                    myEditAdapter.submit(list)
                } else {
                    myEditAdapter.submit(emptyList())
                }
            } catch (e: Exception) {
                myEditAdapter.submit(emptyList())
            }
        }
    }

    private fun loadBuyerReviews() {
        val userId = TokenManager.getMemberId()
        val token = TokenManager.getAccessToken()

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
                            itemTitle = review.itemTitle,
                            createdAt = review.createdAt,
                            reviewerId = review.reviewerId,
                            reviewerNickName = review.reviewerNickName,
                            reviewerProfileImageUrl = review.reviewerProfileImageUrl,
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

    private fun bindProfile() {
        val token = TokenManager.getAccessToken()
        if (token.isNullOrEmpty()) return  // 비로그인 시 기본 이미지/문구 유지

        lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.memberApi.getProfile() // 인터셉터에서 Bearer 붙는다고 가정
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val p = resp.body()!!.result

                    // 닉네임
                    binding.nickname.text = p.nickname ?: "닉네임"

                    // 프로필 이미지
                    val url = p.profileImageUrl
                    if (!url.isNullOrBlank()) {
                        Glide.with(this@MyPageReviewsActivity)
                            .load(url)
                            .placeholder(R.drawable.profile_base)
                            .error(R.drawable.profile_base)
                            .circleCrop()
                            .into(binding.profileImage)
                    } else {
                        binding.profileImage.setImageResource(R.drawable.profile_base)
                    }

                    // (옵션) 인증 지역 표시까지 필요하면 여기에 verifiedRegionNames 처리 추가 가능
                    // val dong = p.verifiedRegionNames.firstOrNull()?.let { extractDong(it) }
                    // binding.regionText.text = dong?.let { "$it 인증 완료" } ?: "인증된 지역 없음"
                } else {
                    Log.e("MyPageReviews", "프로필 응답 실패: ${resp.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("MyPageReviews", "프로필 로드 오류: ${e.message}")
            }
        }
    }


    private fun loadTradeSummary() {
        val userId = TokenManager.getMemberId()
        if (userId == null) {
            // 비로그인 시 0으로 표시
            binding.sellCount.text = "0"
            binding.reviewCount.text = "0"
            binding.ratingSummary.setIsIndicator(true)
            binding.ratingSummary.rating = 0f
            return
        }

        lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.memberApi.getUserTradeHistory(userId)
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val r = resp.body()!!.result

                    // 판매/거래 수
                    binding.sellCount.text = r.totalDealsCount.toString()

                    // 리뷰 수
                    binding.reviewCount.text = r.reviewCount.toString()

                    // 별점: totalRating이 '합계'인지 '평균'인지 모호하니 안전하게 처리
                    val avg = when {
                        r.reviewCount <= 0 -> 0f
                        r.totalRating > 5f -> (r.totalRating / r.reviewCount) // 합계로 내려온 경우
                        else -> r.totalRating                                // 이미 평균인 경우
                    }.coerceIn(0f, 5f)

                    binding.ratingSummary.setIsIndicator(true)
                    binding.ratingSummary.rating = avg
                } else {
                    // 실패 시 기본값
                    binding.sellCount.text = "0"
                    binding.reviewCount.text = "0"
                    binding.ratingSummary.setIsIndicator(true)
                    binding.ratingSummary.rating = 0f

                    Log.e("TradeSummary", "응답 실패: ${resp.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // 네트워크 오류 시 기본값
                binding.sellCount.text = "0"
                binding.reviewCount.text = "0"
                binding.ratingSummary.setIsIndicator(true)
                binding.ratingSummary.rating = 0f

                Log.e("TradeSummary", "오류: ${e.message}")
            }
        }
    }
}