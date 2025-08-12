package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.data.nwonsaved.NwonSavedRepository
import com.example.onenthapp.databinding.ActivityMyReviewBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class MyReviewActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MyReviewActivity"
    }

    private lateinit var binding: ActivityMyReviewBinding
    private lateinit var pendingReviewAdapter: NwonSavedItemAdapter
    private val repository = NwonSavedRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔄 binding 초기화
        binding = ActivityMyReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            Log.d(TAG, "onCreate 시작")

            setupToolbar()
            setupRecyclerViews()
            setupClickListeners()
            fetchPendingReviews()
            fetchNwonSavedDate()

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
    try {
        // 기존 작성된 후기 RecyclerView
        val reviewRecyclerView = findViewById<RecyclerView>(R.id.reviewRecyclerView)
        reviewRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 예시 데이터 (기존 후기)
        val reviews = listOf(
            ReviewData("abced", 5, "정말 친절했어요!"),
            ReviewData("dfsfg", 4, "빠른 거래 감사합니다"),
            ReviewData("jeongmin", 3, "상품이랑 조금 달랐어요"),
        )

        reviewRecyclerView.adapter = ReviewAdapter(reviews)

        // 작성 대기 중인 후기 RecyclerView 설정
        val editReviewRecyclerView = findViewById<RecyclerView>(R.id.editReviewRecyclerView)
        pendingReviewAdapter = NwonSavedItemAdapter()
        editReviewRecyclerView.layoutManager = LinearLayoutManager(this)
        editReviewRecyclerView.adapter = pendingReviewAdapter

        Log.d(TAG, "RecyclerView 초기화 완료")
    } catch (e: Exception) {
        Log.e(TAG, "RecyclerView 설정 오류: ${e.message}", e)
    }
}

private fun setupClickListeners() {
    // 거래 후기 제목 클릭 시 BuyerReview 페이지로 이동
    val titleText = findViewById<TextView>(R.id.buyerReviewTitle)
    titleText.setOnClickListener {
        val intent = Intent(this, BuyerReview::class.java)
        // 예시로 첫 번째 리뷰 데이터 전송 (실제로는 동적 데이터 사용)
        intent.putExtra("name", "abced")
        intent.putExtra("rating", 5)
        intent.putExtra("text", "정말 친절했어요!")
        startActivity(intent)
    }

    // 후기를 작성할래요 클릭 시 EditMyReviewActivity로 이동
    val editMyReviewText = findViewById<TextView>(R.id.editMyReviewText)
    editMyReviewText.setOnClickListener {
        val intent = Intent(this, EditMyReviewActivity::class.java)
        startActivity(intent)
    }
}

    private fun fetchNwonSavedDate() {
        lifecycleScope.launch {
            try {
                val response = repository.lookNwonSaved()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    result?.let {
                        Log.d(TAG, "거래 요약 데이터 수신 성공")

                        // 숫자 포맷 함수 사용 (콤마 등)
                        fun formatNumber(value: Int): String {
                            return NumberFormat.getNumberInstance(Locale.getDefault()).format(value)
                        }

                        // 🟢 같이 사요 데이터
                        binding.purchaseDealCountText.text =
                            formatNumber(it.purchaseDealHistory.totalDealCount)
                        binding.purchaseDealAmountText.text =
                            formatNumber(it.purchaseDealHistory.totalDealAmount)

                        // 🔵 함께 나눠요 데이터
                        binding.shareDealCountText.text =
                            formatNumber(it.shareDealHistory.totalDealCount)
                        binding.shareDealAmountText.text =
                            formatNumber(it.shareDealHistory.totalDealAmount)
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "알 수 없는 오류"
                    Log.e(TAG, "거래 요약 정보 응답 실패: $errorMsg")
                    Toast.makeText(this@MyReviewActivity, "거래 정보 불러오기 실패: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchNwonSavedData 예외 발생", e)
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

                    pendingItems.forEachIndexed { index, item ->
                        Log.d(TAG, "대기 후기 $index: ID=${item.itemId}, 이름=${item.itemName}, 타입=${item.itemType}")
                    }

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
