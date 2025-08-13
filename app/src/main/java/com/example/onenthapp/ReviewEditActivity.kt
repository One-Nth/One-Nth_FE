package com.example.onenthapp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.onenthapp.data.DeleteReviewImageRequest
import com.example.onenthapp.data.ReviewBody
import com.example.onenthapp.data.ReviewImage
import com.example.onenthapp.databinding.EditMyReviewBinding
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ReviewEditActivity : AppCompatActivity() {

    private lateinit var binding: EditMyReviewBinding
    private val api = RetrofitInstance.reviewApi

    private val selectedImageUris = mutableListOf<Uri>()   // 아직 서버에 안올린 신규
    private val existingImageList = mutableListOf<ReviewImage>() // 서버에 있는 기존(삭제 가능)
    private val deletedImageIds = mutableListOf<Long>()

    private var isEditMode = false
    private var canEdit = true

    private val MAX_IMAGES = 5

    // 여러 장 선택
    private val pickImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNullOrEmpty()) return@registerForActivityResult

            val available = (MAX_IMAGES - totalImageCount()).coerceAtLeast(0)
            if (available <= 0) {
                Toast.makeText(this, "이미지는 최대 ${MAX_IMAGES}장까지 첨부할 수 있어요.", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            val toAdd = uris.take(available)
            selectedImageUris.addAll(toAdd)

            if (uris.size > available) {
                Toast.makeText(this, "최대 ${MAX_IMAGES}장까지만 추가돼요. 일부 이미지는 제외됐어요.", Toast.LENGTH_SHORT).show()
            }

            showAllImages() // 내부에서 카운터/버튼상태 갱신됨
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditMyReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val reviewId = intent.getLongExtra("reviewId", -1)
        val itemType = intent.getStringExtra("itemType") ?: ""
        canEdit = intent.getBooleanExtra("canEdit", true)

        val displayNickname = intent.getStringExtra("displayNickname")
        val displayProfileUrl = intent.getStringExtra("displayProfileUrl")

        if (canEdit) {
            setMyProfile()
        } else {
            displayNickname?.let { binding.reviewerNameDetail2.text = it }
            runCatching { binding.root.findViewById<ImageView>(R.id.profileImage2) }.getOrNull()?.let { iv ->
                if (!displayProfileUrl.isNullOrBlank()) {
                    Glide.with(this).load(displayProfileUrl)
                        .placeholder(R.drawable.profile_base)
                        .error(R.drawable.profile_base)
                        .circleCrop()
                        .into(iv)
                }
            }
        }

        if (reviewId == -1L || itemType.isBlank()) {
            Toast.makeText(this, "잘못된 접근입니다", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        binding.topAppBar.setNavigationOnClickListener { finish() }

        if (!canEdit) {
            binding.editButton.visibility = View.GONE
            binding.addImageButton.visibility = View.GONE
            binding.addImageButton.isEnabled = false
            binding.ratingBar.setIsIndicator(true)
            binding.reviewTextDetail2.isEnabled = false
            setEditMode(false)
        }

        // + 버튼 클릭 (항상 보이되, 초과면 토스트만)
        binding.addImageButton.setOnClickListener {
            if (!isEditMode || !canEdit) return@setOnClickListener
            if (totalImageCount() >= MAX_IMAGES) {
                Toast.makeText(this, "최대 ${MAX_IMAGES}장까지 가능해요.", Toast.LENGTH_SHORT).show()
            } else {
                pickImagesLauncher.launch("image/*")
            }
        }

        // 편집 / 완료
        binding.editButton.setOnClickListener {
            if (!canEdit) return@setOnClickListener
            if (isEditMode) {
                lifecycleScope.launch {
                    deleteSelectedImages(reviewId, itemType)
                    if (selectedImageUris.isNotEmpty()) uploadReviewImages(selectedImageUris)
                    updateReviewTextAndRate(reviewId, itemType)
                    Toast.makeText(this@ReviewEditActivity, "수정 완료", Toast.LENGTH_SHORT).show()
                    finish()
                }
                setEditMode(false)
            } else {
                setEditMode(true)
            }
        }

        // 상세 로드
        lifecycleScope.launch {
            try {
                val resp = api.getReviewDetail(reviewId, itemType)
                val body = resp.body()
                if (resp.isSuccessful && body?.isSuccess == true) {
                    val review = body.result

                    if (!displayNickname.isNullOrBlank()) {
                        binding.reviewerNameDetail2.text = displayNickname
                    } else if (canEdit) {
                        binding.reviewerNameDetail2.text = TokenManager.getNickname() ?: "나"
                    }
                    binding.productNameText2.text = review.itemTitle.ifBlank { "상품명 없음" }   // ✅ 제목
                    binding.ratingBar.rating = review.rate.toFloat()
                    binding.reviewTextDetail2.setText(review.content)

                    existingImageList.clear()
                    existingImageList.addAll(review.reviewImageList.map { ReviewImage(it.reviewImageId, it.imageUrl) })

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

    private fun setMyProfile() {
        val cachedNick = TokenManager.getNickname()
        binding.reviewerNameDetail2.text = if (!cachedNick.isNullOrBlank()) cachedNick else "나"

        val token = TokenManager.getAccessToken() ?: return
        lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.memberApi.getProfile()
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val r = resp.body()!!.result
                    val nick = r.nickname ?: "나"
                    binding.reviewerNameDetail2.text = nick
                    TokenManager.saveNickname(nick)

                    val url = r.profileImageUrl
                    runCatching { binding.root.findViewById<ImageView>(R.id.profileImage2) }.getOrNull()?.let { iv ->
                        if (!url.isNullOrBlank()) {
                            Glide.with(this@ReviewEditActivity).load(url)
                                .placeholder(R.drawable.profile_base).error(R.drawable.profile_base)
                                .circleCrop().into(iv)
                        } else iv.setImageResource(R.drawable.profile_base)
                    }
                }
            } catch (_: Exception) { /* ignore */ }
        }
    }

    private fun setEditMode(enabled: Boolean) {
        if (enabled && !canEdit) return
        isEditMode = enabled
        binding.editButton.setImageResource(
            if (enabled) R.drawable.completebtn_editreview else R.drawable.editbutton
        )

        val editable = enabled && canEdit
        binding.reviewTextDetail2.isEnabled = editable
        binding.reviewTextDetail2.isFocusable = editable
        binding.reviewTextDetail2.isFocusableInTouchMode = editable
        binding.ratingBar.setIsIndicator(!editable)

        // + 버튼은 항상 보이되, 편집 아닐 땐 숨김
        binding.addImageButton.visibility = if (editable) View.VISIBLE else View.GONE

        showAllImages() // 카운터/상태 재반영
    }

    /** 컨테이너 그리기 + 카운터 & 버튼 상태 갱신 */
    private fun showAllImages() {
        val container = binding.imageContainer
        container.removeAllViews()

        // 항상 맨 앞에 + 버튼
        container.addView(binding.addImageButton)

        // 기존 이미지
        existingImageList.forEach { image ->
            val item = layoutInflater.inflate(R.layout.item_edit_review_image, container, false) as FrameLayout
            val iv = item.findViewById<ImageView>(R.id.imageView)
            val del = item.findViewById<ImageView>(R.id.deleteButton)

            Glide.with(this).load(image.url).into(iv)

            del.visibility = if (isEditMode && canEdit) View.VISIBLE else View.GONE
            del.setOnClickListener {
                if (!canEdit) return@setOnClickListener
                deletedImageIds.add(image.id)
                existingImageList.remove(image)
                showAllImages()
            }
            container.addView(item)
        }

        // 새 이미지
        selectedImageUris.forEach { uri ->
            val item = layoutInflater.inflate(R.layout.item_edit_review_image, container, false) as FrameLayout
            val iv = item.findViewById<ImageView>(R.id.imageView)
            val del = item.findViewById<ImageView>(R.id.deleteButton)

            iv.setImageURI(uri)

            del.visibility = if (isEditMode && canEdit) View.VISIBLE else View.GONE
            del.setOnClickListener {
                if (!canEdit) return@setOnClickListener
                selectedImageUris.remove(uri)
                showAllImages()
            }
            container.addView(item)
        }

        // ▼ 카운터/버튼 상태 업데이트
        updateCounterAndButton()
    }

    private fun updateCounterAndButton() {
        val count = totalImageCount()
        binding.tvImageCount.text = "$count/$MAX_IMAGES"

        // 꽉 찼을 때도 버튼은 보이되 클릭만 막고 흐리게
        val canAddMore = (count < MAX_IMAGES) && isEditMode && canEdit
        binding.addImageButton.isEnabled = canAddMore
        binding.addImageButton.alpha = if (canAddMore) 1f else 0.5f
    }

    private fun totalImageCount(): Int = existingImageList.size + selectedImageUris.size

    private suspend fun deleteSelectedImages(reviewId: Long, itemType: String) {
        if (!canEdit || deletedImageIds.isEmpty()) return
        try {
            val req = DeleteReviewImageRequest(imageIds = deletedImageIds)
            val resp = api.deleteReviewImages(reviewId, itemType, req)
            if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                Toast.makeText(this, "이미지 삭제 완료", Toast.LENGTH_SHORT).show()
                deletedImageIds.clear()
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
            val resp = api.getReviewDetail(reviewId, itemType)
            val body = resp.body()
            if (resp.isSuccessful && body?.isSuccess == true) {
                val review = body.result
                existingImageList.clear()
                existingImageList.addAll(review.reviewImageList.map { ReviewImage(it.reviewImageId, it.imageUrl) })
                showAllImages()
            }
        } catch (_: Exception) { }
    }

    private fun uploadReviewImages(imageUris: List<Uri>) {
        if (!canEdit) return
        val reviewId = intent.getLongExtra("reviewId", -1)
        val itemType = intent.getStringExtra("itemType") ?: ""
        val imageParts = prepareImageParts(imageUris)

        lifecycleScope.launch {
            try {
                val resp = api.uploadReviewImages(reviewId, itemType, imageParts)
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
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
                contentResolver.openInputStream(uri)?.use { input ->
                    val bytes = input.readBytes()
                    val body = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    parts += MultipartBody.Part.createFormData("images", "image_$index.jpg", body)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
        return parts
    }

    private suspend fun updateReviewTextAndRate(reviewId: Long, itemType: String) {
        if (!canEdit) return
        val content = binding.reviewTextDetail2.text.toString()
        val rate = binding.ratingBar.rating.toInt()
        val req = ReviewBody(content, rate)
        try {
            val resp = api.updateReviewContentAndRate(reviewId, itemType, req)
            if (!resp.isSuccessful || resp.body()?.isSuccess != true) {
                Toast.makeText(this, "리뷰 본문/별점 수정 실패", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "리뷰 수정 네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
