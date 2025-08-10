package com.example.onenthapp

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.onenthapp.data.DeleteReviewImageRequest
import com.example.onenthapp.data.ReviewBody
import com.example.onenthapp.data.ReviewDetailResult
import com.example.onenthapp.data.ReviewImage
import com.example.onenthapp.databinding.EditMyReviewBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ReviewEditActivity : AppCompatActivity() {

    private lateinit var binding: EditMyReviewBinding
    private val api = RetrofitInstance.reviewApi

    private val selectedImageUris = mutableListOf<Uri>()
    private val existingImageList = mutableListOf<ReviewImage>() // 이미지 ID 포함
    private val deletedImageIds = mutableListOf<Long>() // 삭제할 이미지 ID들

    private var isEditMode = false

    private val pickImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (!uris.isNullOrEmpty()) {
                selectedImageUris.clear()
                selectedImageUris.addAll(uris)
                showAllImages()
            }
        }

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

        binding.topAppBar.setNavigationOnClickListener { finish() }

        binding.editButton.setOnClickListener {
            if (isEditMode) {
                lifecycleScope.launch {
                    // 1. 삭제 먼저 반영
                    deleteSelectedImages(reviewId, itemType)

                    // 2. 이미지 추가가 있다면 업로드
                    if (selectedImageUris.isNotEmpty()) {
                        uploadReviewImages(selectedImageUris)
                    }

                    updateReviewTextAndRate(reviewId, itemType)

                    // 3. 이미지 추가 없더라도 리뷰 내용 수정 포함될 수 있으니 성공 메시지
                    Toast.makeText(this@ReviewEditActivity, "수정 완료", Toast.LENGTH_SHORT).show()
                    finish()
                }
                setEditMode(false)
            } else {
                setEditMode(true)
            }
        }


        // 후기 상세 조회 API 호출
        lifecycleScope.launch {
            try {
                val response = api.getReviewDetail(reviewId, itemType)
                val body = response.body()
                if (response.isSuccessful && body != null && body.isSuccess) {
                    val review = body.result

                    binding.reviewerNameDetail2.text = "나"
                    binding.productNameText2.text = "상품 ID: ${review.itemId}"
                    binding.ratingBar.rating = review.rate.toFloat()
                    binding.reviewTextDetail2.setText(review.content)

                    existingImageList.clear()
                    existingImageList.addAll(
                        review.reviewImageList.map {
                            ReviewImage(id = it.reviewImageId, url = it.imageUrl)
                        }
                    )

                    showAllImages()
                    setEditMode(false)
                } else {
                    Toast.makeText(this@ReviewEditActivity, "리뷰 불러오기 실패", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ReviewEditActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        val buttonRes = if (enabled) R.drawable.completebtn_editreview else R.drawable.editbutton
        binding.editButton.setImageResource(buttonRes)

        binding.reviewTextDetail2.isEnabled = enabled
        binding.reviewTextDetail2.isFocusable = enabled
        binding.reviewTextDetail2.isFocusableInTouchMode = enabled

        binding.addImageButton.isEnabled = enabled
        binding.ratingBar.setIsIndicator(!enabled)

        showAllImages()
    }


    private fun showAllImages() {
        val container = binding.imageContainer
        container.removeAllViews()

        // addImageButton 한 번만 설정 및 추가
        binding.addImageButton.apply {
            visibility = if (isEditMode) View.VISIBLE else View.GONE
            setOnClickListener {
                if (isEditMode) pickImagesLauncher.launch("image/*")
            }
        }
        container.addView(binding.addImageButton)

        // 기존 이미지 표시
        existingImageList.forEach { image ->
            val imageLayout = layoutInflater.inflate(R.layout.item_edit_review_image, container, false) as FrameLayout
            val imageView = imageLayout.findViewById<ImageView>(R.id.imageView)
            val removeButton = imageLayout.findViewById<ImageView>(R.id.deleteButton)
            Glide.with(this).load(image.url).into(imageView)
            removeButton.visibility = if (isEditMode) View.VISIBLE else View.GONE
            removeButton.setOnClickListener {
                deletedImageIds.add(image.id)
                existingImageList.remove(image)
                showAllImages()
            }
            container.addView(imageLayout)
        }

        // 새로 추가된 이미지 표시
        selectedImageUris.forEach { uri ->
            val imageLayout = layoutInflater.inflate(R.layout.item_edit_review_image, container, false) as FrameLayout
            val imageView = imageLayout.findViewById<ImageView>(R.id.imageView)
            val removeButton = imageLayout.findViewById<ImageView>(R.id.deleteButton)
            imageView.setImageURI(uri)
            removeButton.visibility = if (isEditMode) View.VISIBLE else View.GONE
            removeButton.setOnClickListener {
                selectedImageUris.remove(uri)
                showAllImages()
            }
            container.addView(imageLayout)
        }
    }

    private suspend fun deleteSelectedImages(reviewId: Long, itemType: String) {
        if (deletedImageIds.isEmpty()) return
        try {
            val request = DeleteReviewImageRequest(imageIds = deletedImageIds)
            val response = api.deleteReviewImages(reviewId, itemType, request)
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Toast.makeText(this, "이미지 삭제 완료", Toast.LENGTH_SHORT).show()
                // 삭제 목록 초기화
                deletedImageIds.clear()

                // ✅ 서버에서 최신 상태 가져와서 반영
                refreshReviewDetail(reviewId, itemType)
            } else {
                Toast.makeText(this, "이미지 삭제 실패", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "이미지 삭제 오류: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun refreshReviewDetail(reviewId: Long, itemType: String) {
        try {
            val response = api.getReviewDetail(reviewId, itemType)
            val body = response.body()
            if (response.isSuccessful && body != null && body.isSuccess) {
                val review = body.result
                existingImageList.clear()
                existingImageList.addAll(
                    review.reviewImageList.map {
                        ReviewImage(id = it.reviewImageId, url = it.imageUrl)
                    }
                )

                showAllImages()
            }
        } catch (_: Exception) {
        }
    }


    private fun uploadReviewImages(imageUris: List<Uri>) {
        val reviewId = intent.getLongExtra("reviewId", -1)
        val itemType = intent.getStringExtra("itemType") ?: ""
        val imageParts = prepareImageParts(imageUris)

        lifecycleScope.launch {
            try {
                val response = api.uploadReviewImages(
                    reviewId = reviewId,
                    itemType = itemType,
                    images = imageParts
                )
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(this@ReviewEditActivity, "수정 완료", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ReviewEditActivity, "업로드 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ReviewEditActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prepareImageParts(imageUris: List<Uri>): List<MultipartBody.Part> {
        val parts = mutableListOf<MultipartBody.Part>()
        imageUris.forEachIndexed { index, uri ->
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val fileBytes = inputStream?.readBytes()
                inputStream?.close()
                if (fileBytes != null) {
                    val requestBody = fileBytes.toRequestBody("image/*".toMediaTypeOrNull())
                    val fileName = "image_$index.jpg"
                    val part = MultipartBody.Part.createFormData("images", fileName, requestBody)
                    parts.add(part)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return parts
    }

    private suspend fun updateReviewTextAndRate(reviewId: Long, itemType: String) {
        val content = binding.reviewTextDetail2.text.toString()
        var rate = binding.ratingBar.rating.toInt()

        val request = ReviewBody(content, rate)

        try {
            val response = api.updateReviewContentAndRate(reviewId, itemType, request)
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Log.d("ReviewEdit", "본문/별점 수정 완료")
            } else {
                Toast.makeText(this, "리뷰 본문/별점 수정 실패", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "리뷰 수정 네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
