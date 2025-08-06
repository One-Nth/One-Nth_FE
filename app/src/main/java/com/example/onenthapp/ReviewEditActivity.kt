package com.example.onenthapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.onenthapp.data.ReviewDetailResult
import com.example.onenthapp.databinding.EditMyReviewBinding
import kotlinx.coroutines.launch

class ReviewEditActivity : AppCompatActivity() {

    private lateinit var binding: EditMyReviewBinding
    private val api = RetrofitInstance.reviewApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditMyReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val reviewId = intent.getLongExtra("reviewId", -1)
        val itemType = intent.getStringExtra("itemType") ?: ""

        if (reviewId == -1L || itemType.isBlank()) {
            Toast.makeText(this, "잘못된 접근입니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ⛳️ Toolbar 뒤로가기
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        // ⭐️ 후기 상세 조회 API 호출
        lifecycleScope.launch {
            try {
                val response = api.getReviewDetail(reviewId, itemType)
                val body = response.body()
                if (response.isSuccessful && body != null && body.isSuccess) {
                    val review = body.result

                    // 닉네임
                    binding.reviewerNameDetail2.text = "나"

                    // 상품명 (이름이 없다면 itemId 표시 등 대체 로직 필요)
                    binding.productNameText2.text = "상품 ID: ${review.itemId}"

                    // 별점 텍스트로 표시 (예: ★★★☆☆)
                    val stars = "★★★★★".substring(0, review.rate) +
                            "☆☆☆☆☆".substring(0, 5 - review.rate)
                    binding.starRatingDetail2.text = stars

                    // 후기 내용
                    binding.reviewTextDetail2.text = review.content

                    // 후기 이미지 리스트 동적 추가
                    val imageContainer = binding.imageContainer
                    imageContainer.removeAllViews()

                    review.reviewImageList.forEach { imageUrl ->
                        val imageView = ImageView(this@ReviewEditActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                98.dpToPx(),
                                93.dpToPx()
                            ).apply {
                                marginEnd = 20.dpToPx()
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            setBackgroundResource(R.color.image_placeholder)
                        }

                        Glide.with(this@ReviewEditActivity)
                            .load(imageUrl)
                            .into(imageView)

                        imageContainer.addView(imageView)
                    }

                } else {
                    Toast.makeText(this@ReviewEditActivity, "리뷰 불러오기 실패", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ReviewEditActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // ✏️ 수정 버튼 클릭 처리
        binding.editButton.setOnClickListener {
            // 수정 기능은 여기서 구현
        }
    }

    // 🔧 dp to px 확장 함수
    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()
}
