package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.data.MyReview
import com.example.onenthapp.databinding.ActivityMypageReviewsBinding
import com.example.onenthapp.feature.mypage.ReviewEditActivity
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch

class MyPageReviewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMypageReviewsBinding
    private lateinit var adapter: BuyerReviewAdapter
    private val reviewApi = RetrofitInstance.reviewApi
    private lateinit var myEditAdapter: MyOwnReviewEditAdapter
    private lateinit var saleItemsAdapter: SellerItemAdapter

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

        // 판매물품 전체보기 버튼 클릭 리스너
        binding.btnGoAllItems.setOnClickListener {
            startActivity(Intent(this, MySaleItemsActivity::class.java))
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

        // 판매물품 리사이클러뷰 설정 (기존 판매자 프로필과 동일하게 SellerItemAdapter 사용, 카테고리 칩 숨김)
        saleItemsAdapter = SellerItemAdapter(hideCategory = true)
        binding.saleItemsRecyclerView.adapter = saleItemsAdapter
        binding.saleItemsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.saleItemsRecyclerView.minimumHeight = (220 * resources.displayMetrics.density).toInt()

        loadBuyerReviews()
        bindProfile()
        loadTradeSummary()
        loadMyWrittenReviews()
        loadMySaleItems()
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
                val resp = RetrofitInstance.memberApi.getProfile() // /api/user-settings/profile 이어야 함
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

                    // ✅ 인증 지역
                    val firstRegionFull = p.verifiedRegionNames?.firstOrNull()
                    val dongOnly = extractDong(firstRegionFull) ?: firstRegionFull

                    binding.regionText.apply {
                        if (!dongOnly.isNullOrBlank()) {
                            text = "$dongOnly 인증완료"
                            setTextColor(getColor(R.color.main_green_2))
                            visibility = View.VISIBLE
                        } else {
                            text = "인증된 지역 없음"
                            setTextColor(getColor(R.color.main_green_2))
                            visibility = View.VISIBLE
                        }
                    }

                } else {
                    // 실패 시 지역 기본값
                    binding.regionText.text = "인증된 지역 없음"
                }
            } catch (e: Exception) {
                binding.regionText.text = "인증된 지역 없음"
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

    private fun loadMySaleItems() {
        val token = TokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            // 비로그인 시 빈 리스트 표시
            saleItemsAdapter.updateList(emptyList())
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.memberApi.getMyItems("Bearer $token", page = 1, size = 10)
                if (response.isSuccess && response.result != null) {
                    // MyPostProductItem을 SellerItem으로 변환
                    val sellerItems = response.result.items.map { item ->
                        com.example.onenthapp.data.SellerItem(
                            id = item.itemId,
                            name = item.productName,
                            status = "DEFAULT", // MyPostProductItem에는 status가 없으므로 기본값
                            price = item.price.toInt(),
                            itemCategory = item.itemType, // PURCHASE, SHARE 등을 카테고리로 사용
                            purchaseMethod = "ONLINE", // 기본값
                            thumbnailUrl = item.imageUrl ?: ""
                        )
                    }
                    saleItemsAdapter.updateList(sellerItems)
                } else {
                    saleItemsAdapter.updateList(emptyList())
                    Log.e("MyPageReviews", "판매물품 불러오기 실패: ${response.message}")
                }
            } catch (e: Exception) {
                saleItemsAdapter.updateList(emptyList())
                Log.e("MyPageReviews", "판매물품 네트워크 오류: ${e.message}")
            }
        }
    }

    private fun extractDong(full: String?): String? {
        if (full.isNullOrBlank()) return null

        // 구분자 통일 후 토큰화
        val tokens = full.replace(",", " ")
            .replace("·", " ")
            .split(" ")
            .filter { it.isNotBlank() }

        // 뒤에서부터 동/가/읍/면/리 같은 말단 행정동 찾기
        val suffixes = listOf("동", "가", "읍", "면", "리")
        return tokens.asReversed().firstOrNull { t -> suffixes.any { t.endsWith(it) } }
    }

}