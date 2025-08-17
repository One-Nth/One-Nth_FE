package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.data.nwonsaved.NwonSavedRepository
import com.example.onenthapp.databinding.ActivityMyReviewBinding
import com.example.onenthapp.RetrofitInstance.reviewApi
import com.example.onenthapp.data.MyReview
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import android.util.TypedValue

class MyReviewActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MyReviewActivity"
    }

    private lateinit var binding: ActivityMyReviewBinding

    private lateinit var pendingReviewAdapter: NwonSavedItemAdapter
    private lateinit var buyerAdapter: BuyerReviewAdapter

    private val repository = NwonSavedRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            Log.d(TAG, "onCreate 시작")

            setupToolbar()
            setupRecyclerViews()
            setupClickListeners()

            fetchPendingReviews()
            fetchNwonSavedDate()
            fetchBuyerReviews()

            Log.d(TAG, "onCreate 정상 종료")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate 오류: ${e.message}", e)
            Toast.makeText(this, "화면 초기화 중 오류 발생", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerViews() {
        // 1) 구매자 거래 후기 (가로)
        buyerAdapter = BuyerReviewAdapter()
        binding.buyerReviewRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MyReviewActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = buyerAdapter
            setHasFixedSize(true)
        }

        // 스크롤바 연동 (프레임 레이아웃 350dp 기준)
        val scrollContainerWidthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 350f, resources.displayMetrics
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

        // 2) 작성 대기 후기 (세로)
        pendingReviewAdapter = NwonSavedItemAdapter { item ->
            val intent = Intent(this, WriteReviewActivity::class.java).apply {
                when (item.itemType.uppercase()) {
                    "PURCHASE" -> putExtra("purchaseItemId", item.itemId)
                    "SHARING" -> putExtra("sharingItemId", item.itemId)
                }
                putExtra("itemName", item.itemName)
                putExtra("itemImageUrl", item.itemImageUrl)
            }
            startActivity(intent)
        }


        binding.editReviewRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MyReviewActivity)
            adapter = pendingReviewAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.btnGoAllReviews.setOnClickListener {
            val userId = TokenManager.getMemberId() ?: return@setOnClickListener
            startActivity(Intent(this, BuyerReviewDetailActivity::class.java).apply {
                putExtra("userId", userId)
            })
        }
    }

    private fun fetchBuyerReviews() {
        val userId = TokenManager.getMemberId()
        val token = TokenManager.getAccessToken()

        if (userId == null || token.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = reviewApi.getUserReviews(userId, "Bearer $token")
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val reviewList = response.body()!!.result.reviewList.map { r ->
                        MyReview(
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
                    Log.e(TAG, "구매자 후기 응답 실패: ${response.errorBody()?.string()}")
                    Toast.makeText(this@MyReviewActivity, "구매자 거래 후기를 불러오지 못했습니다", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchBuyerReviews 오류: ${e.message}", e)
                Toast.makeText(this@MyReviewActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchNwonSavedDate() {
        lifecycleScope.launch {
            try {
                val response = repository.lookNwonSaved()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    response.body()?.result?.let {
                        Log.d(TAG, "거래 요약 데이터 수신 성공")

                        fun formatNumber(value: Int): String =
                            NumberFormat.getNumberInstance(Locale.getDefault()).format(value)

                        binding.purchaseDealCountText.text = formatNumber(it.purchaseDealHistory.totalDealCount)
                        binding.purchaseDealAmountText.text = formatNumber(it.purchaseDealHistory.totalDealAmount)
                        binding.shareDealCountText.text = formatNumber(it.shareDealHistory.totalDealCount)
                        binding.shareDealAmountText.text = formatNumber(it.shareDealHistory.totalDealAmount)
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "알 수 없는 오류"
                    Log.e(TAG, "거래 요약 정보 응답 실패: $errorMsg")
                    Toast.makeText(this@MyReviewActivity, "거래 정보 불러오기 실패: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchNwonSavedDate 예외 발생", e)
                Toast.makeText(this@MyReviewActivity, "서버 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchPendingReviews() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "작성 대기 중인 후기 API 요청 시작")

                val pendingResponse = repository.lookMyHistoryItem("pending")
                if (pendingResponse.isSuccessful && pendingResponse.body()?.isSuccess == true) {
                    val pendingItems = pendingResponse.body()?.result ?: emptyList()
                    Log.d(TAG, "작성 대기 후기 ${pendingItems.size}개 수신")

                    if (pendingItems.isEmpty()) {
                        Toast.makeText(this@MyReviewActivity, "작성할 후기가 없습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        pendingReviewAdapter.submitList(pendingItems)
                        Log.d(TAG, "대기 후기 어댑터에 데이터 전달 완료")
                    }
                } else {
                    val errorMsg = pendingResponse.body()?.message ?: "알 수 없는 오류"
                    Log.e(TAG, "작성 대기 후기 응답 실패: $errorMsg")
                    Toast.makeText(this@MyReviewActivity, "작성 대기 후기 불러오기 실패: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchPendingReviews 예외 발생", e)
                Toast.makeText(this@MyReviewActivity, "서버 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
