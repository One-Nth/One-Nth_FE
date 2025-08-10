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

//class ReviewEditActivity : AppCompatActivity() {
//
//    private lateinit var binding: EditMyReviewBinding
//    private val api = RetrofitInstance.reviewApi
//
//    private val selectedImageUris = mutableListOf<Uri>()
//    private val existingImageList = mutableListOf<ReviewImage>() // 이미지 ID 포함
//    private val deletedImageIds = mutableListOf<Long>() // 삭제할 이미지 ID들
//
//    private var isEditMode = false
//
//    private val pickImagesLauncher =
//        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
//            if (!uris.isNullOrEmpty()) {
//                selectedImageUris.clear()
//                selectedImageUris.addAll(uris)
//                showAllImages()
//            }
//        }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = EditMyReviewBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val reviewId = intent.getLongExtra("reviewId", -1)
//        val itemType = intent.getStringExtra("itemType") ?: ""
//
//        setMyProfile()
//
//        if (reviewId == -1L || itemType.isBlank()) {
//            Toast.makeText(this, "잘못된 접근입니다", Toast.LENGTH_SHORT).show()
//            finish()
//            return
//        }
//
//        binding.topAppBar.setNavigationOnClickListener { finish() }
//
//        binding.editButton.setOnClickListener {
//            if (isEditMode) {
//                lifecycleScope.launch {
//                    // 1. 삭제 먼저 반영
//                    deleteSelectedImages(reviewId, itemType)
//
//                    // 2. 이미지 추가가 있다면 업로드
//                    if (selectedImageUris.isNotEmpty()) {
//                        uploadReviewImages(selectedImageUris)
//                    }
//
//                    updateReviewTextAndRate(reviewId, itemType)
//
//                    // 3. 이미지 추가 없더라도 리뷰 내용 수정 포함될 수 있으니 성공 메시지
//                    Toast.makeText(this@ReviewEditActivity, "수정 완료", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//                setEditMode(false)
//            } else {
//                setEditMode(true)
//            }
//        }
//
//
//        // 후기 상세 조회 API 호출
//        lifecycleScope.launch {
//            try {
//                val response = api.getReviewDetail(reviewId, itemType)
//                val body = response.body()
//                if (response.isSuccessful && body != null && body.isSuccess) {
//                    val review = body.result
//
//                    binding.reviewerNameDetail2.text = TokenManager.getNickname() ?: "나"
//                    binding.productNameText2.text = "상품 ID: ${review.itemId}"
//                    binding.ratingBar.rating = review.rate.toFloat()
//                    binding.reviewTextDetail2.setText(review.content)
//
//                    existingImageList.clear()
//                    existingImageList.addAll(
//                        review.reviewImageList.map {
//                            ReviewImage(id = it.reviewImageId, url = it.imageUrl)
//                        }
//                    )
//
//                    showAllImages()
//                    setEditMode(false)
//                } else {
//                    Toast.makeText(this@ReviewEditActivity, "리뷰 불러오기 실패", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//            } catch (e: Exception) {
//                Toast.makeText(this@ReviewEditActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }
//    }
//
//    private fun setMyProfile() {
//        // 1) 캐시 우선
//        val cachedNick = TokenManager.getNickname()
//        if (!cachedNick.isNullOrBlank()) {
//            binding.reviewerNameDetail2.text = cachedNick
//        } else {
//            binding.reviewerNameDetail2.text = "나" // 일단 기본값
//        }
//
//        // 2) 프로필로 최신값 보강 (토큰 있으면)
//        val token = TokenManager.getAccessToken()
//        if (token.isNullOrEmpty()) return
//
//        lifecycleScope.launch {
//            try {
//                val resp = RetrofitInstance.memberApi.getProfile()
//                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
//                    val result = resp.body()!!.result
//                    val nick = result.nickname ?: "나"
//                    binding.reviewerNameDetail2.text = nick
//                    TokenManager.saveNickname(nick) // 캐시 업뎃
//
//                    // (옵션) 프로필 이미지도 레이아웃에 있으면 로드
//                    // 예: binding.profileImageDetail2 가 있을 경우
//                    val url = result.profileImageUrl
//                    val iv = runCatching { binding.root.findViewById<ImageView>(R.id.profileImage2) }.getOrNull()
//                    iv?.let {
//                        if (!url.isNullOrBlank()) {
//                            Glide.with(this@ReviewEditActivity)
//                                .load(url)
//                                .placeholder(R.drawable.profile_base)
//                                .error(R.drawable.profile_base)
//                                .circleCrop()
//                                .into(it)
//                        } else {
//                            it.setImageResource(R.drawable.profile_base)
//                        }
//                    }
//                }
//            } catch (_: Exception) {
//                // 네트워크 실패 시 캐시값 유지
//            }
//        }
//    }
//
//
//    private fun setEditMode(enabled: Boolean) {
//        isEditMode = enabled
//        val buttonRes = if (enabled) R.drawable.completebtn_editreview else R.drawable.editbutton
//        binding.editButton.setImageResource(buttonRes)
//
//        binding.reviewTextDetail2.isEnabled = enabled
//        binding.reviewTextDetail2.isFocusable = enabled
//        binding.reviewTextDetail2.isFocusableInTouchMode = enabled
//
//        binding.addImageButton.isEnabled = enabled
//        binding.ratingBar.setIsIndicator(!enabled)
//
//        showAllImages()
//    }
//
//
//    private fun showAllImages() {
//        val container = binding.imageContainer
//        container.removeAllViews()
//
//        // addImageButton 한 번만 설정 및 추가
//        binding.addImageButton.apply {
//            visibility = if (isEditMode) View.VISIBLE else View.GONE
//            setOnClickListener {
//                if (isEditMode) pickImagesLauncher.launch("image/*")
//            }
//        }
//        container.addView(binding.addImageButton)
//
//        // 기존 이미지 표시
//        existingImageList.forEach { image ->
//            val imageLayout = layoutInflater.inflate(R.layout.item_edit_review_image, container, false) as FrameLayout
//            val imageView = imageLayout.findViewById<ImageView>(R.id.imageView)
//            val removeButton = imageLayout.findViewById<ImageView>(R.id.deleteButton)
//            Glide.with(this).load(image.url).into(imageView)
//            removeButton.visibility = if (isEditMode) View.VISIBLE else View.GONE
//            removeButton.setOnClickListener {
//                deletedImageIds.add(image.id)
//                existingImageList.remove(image)
//                showAllImages()
//            }
//            container.addView(imageLayout)
//        }
//
//        // 새로 추가된 이미지 표시
//        selectedImageUris.forEach { uri ->
//            val imageLayout = layoutInflater.inflate(R.layout.item_edit_review_image, container, false) as FrameLayout
//            val imageView = imageLayout.findViewById<ImageView>(R.id.imageView)
//            val removeButton = imageLayout.findViewById<ImageView>(R.id.deleteButton)
//            imageView.setImageURI(uri)
//            removeButton.visibility = if (isEditMode) View.VISIBLE else View.GONE
//            removeButton.setOnClickListener {
//                selectedImageUris.remove(uri)
//                showAllImages()
//            }
//            container.addView(imageLayout)
//        }
//    }
//
//    private suspend fun deleteSelectedImages(reviewId: Long, itemType: String) {
//        if (deletedImageIds.isEmpty()) return
//        try {
//            val request = DeleteReviewImageRequest(imageIds = deletedImageIds)
//            val response = api.deleteReviewImages(reviewId, itemType, request)
//            if (response.isSuccessful && response.body()?.isSuccess == true) {
//                Toast.makeText(this, "이미지 삭제 완료", Toast.LENGTH_SHORT).show()
//                // 삭제 목록 초기화
//                deletedImageIds.clear()
//
//                // ✅ 서버에서 최신 상태 가져와서 반영
//                refreshReviewDetail(reviewId, itemType)
//            } else {
//                Toast.makeText(this, "이미지 삭제 실패", Toast.LENGTH_SHORT).show()
//            }
//        } catch (e: Exception) {
//            Toast.makeText(this, "이미지 삭제 오류: ${e.message}", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private suspend fun refreshReviewDetail(reviewId: Long, itemType: String) {
//        try {
//            val response = api.getReviewDetail(reviewId, itemType)
//            val body = response.body()
//            if (response.isSuccessful && body != null && body.isSuccess) {
//                val review = body.result
//                existingImageList.clear()
//                existingImageList.addAll(
//                    review.reviewImageList.map {
//                        ReviewImage(id = it.reviewImageId, url = it.imageUrl)
//                    }
//                )
//
//                showAllImages()
//            }
//        } catch (_: Exception) {
//        }
//    }
//
//
//    private fun uploadReviewImages(imageUris: List<Uri>) {
//        val reviewId = intent.getLongExtra("reviewId", -1)
//        val itemType = intent.getStringExtra("itemType") ?: ""
//        val imageParts = prepareImageParts(imageUris)
//
//        lifecycleScope.launch {
//            try {
//                val response = api.uploadReviewImages(
//                    reviewId = reviewId,
//                    itemType = itemType,
//                    images = imageParts
//                )
//                if (response.isSuccessful && response.body()?.isSuccess == true) {
//                    Toast.makeText(this@ReviewEditActivity, "수정 완료", Toast.LENGTH_SHORT).show()
//                    finish()
//                } else {
//                    Toast.makeText(this@ReviewEditActivity, "업로드 실패", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: Exception) {
//                Toast.makeText(this@ReviewEditActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun prepareImageParts(imageUris: List<Uri>): List<MultipartBody.Part> {
//        val parts = mutableListOf<MultipartBody.Part>()
//        imageUris.forEachIndexed { index, uri ->
//            try {
//                val inputStream = contentResolver.openInputStream(uri)
//                val fileBytes = inputStream?.readBytes()
//                inputStream?.close()
//                if (fileBytes != null) {
//                    val requestBody = fileBytes.toRequestBody("image/*".toMediaTypeOrNull())
//                    val fileName = "image_$index.jpg"
//                    val part = MultipartBody.Part.createFormData("images", fileName, requestBody)
//                    parts.add(part)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        return parts
//    }
//
//    private suspend fun updateReviewTextAndRate(reviewId: Long, itemType: String) {
//        val content = binding.reviewTextDetail2.text.toString()
//        var rate = binding.ratingBar.rating.toInt()
//
//        val request = ReviewBody(content, rate)
//
//        try {
//            val response = api.updateReviewContentAndRate(reviewId, itemType, request)
//            if (response.isSuccessful && response.body()?.isSuccess == true) {
//                Log.d("ReviewEdit", "본문/별점 수정 완료")
//            } else {
//                Toast.makeText(this, "리뷰 본문/별점 수정 실패", Toast.LENGTH_SHORT).show()
//            }
//        } catch (e: Exception) {
//            Toast.makeText(this, "리뷰 수정 네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
//}


class ReviewEditActivity : AppCompatActivity() {

    private lateinit var binding: EditMyReviewBinding
    private val api = RetrofitInstance.reviewApi

    private val selectedImageUris = mutableListOf<Uri>()
    private val existingImageList = mutableListOf<ReviewImage>() // 이미지 ID 포함
    private val deletedImageIds = mutableListOf<Long>()          // 삭제할 이미지 ID들

    private var isEditMode = false
    private var canEdit = true  // ✅ 인텐트로 받아서 읽기전용/수정가능 분기

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
        canEdit = intent.getBooleanExtra("canEdit", true) // ✅ BuyerReviewAdapter에서는 false로 넘김

        // 목록에서 표시용 닉네임/이미지를 넘겨줄 수 있음(없으면 null)
        val displayNickname = intent.getStringExtra("displayNickname")
        val displayProfileUrl = intent.getStringExtra("displayProfileUrl")

        // 내 후기(수정 가능)일 때만 내 프로필 주입해 헤더 채움
        if (canEdit) {
            setMyProfile()
        } else {
            // 읽기 전용이면 목록에서 넘어온 표시용 값 우선 사용
            displayNickname?.let { binding.reviewerNameDetail2.text = it }
            val iv = runCatching { binding.root.findViewById<ImageView>(R.id.profileImage2) }.getOrNull()
            if (!displayProfileUrl.isNullOrBlank()) {
                iv?.let {
                    Glide.with(this)
                        .load(displayProfileUrl)
                        .placeholder(R.drawable.profile_base)
                        .error(R.drawable.profile_base)
                        .circleCrop()
                        .into(it)
                }
            }
        }

        if (reviewId == -1L || itemType.isBlank()) {
            Toast.makeText(this, "잘못된 접근입니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.topAppBar.setNavigationOnClickListener { finish() }

        // ✅ 읽기 전용이면 편집 UI 비활성화
        if (!canEdit) {
            binding.editButton.visibility = View.GONE
            binding.addImageButton.visibility = View.GONE
            binding.addImageButton.isEnabled = false
            binding.ratingBar.setIsIndicator(true)
            binding.reviewTextDetail2.isEnabled = false
            setEditMode(false)
        }

        binding.editButton.setOnClickListener {
            if (!canEdit) return@setOnClickListener
            if (isEditMode) {
                lifecycleScope.launch {
                    // 1) 삭제 먼저 반영
                    deleteSelectedImages(reviewId, itemType)
                    // 2) 이미지 추가 업로드
                    if (selectedImageUris.isNotEmpty()) {
                        uploadReviewImages(selectedImageUris)
                    }
                    // 3) 본문/별점 수정
                    updateReviewTextAndRate(reviewId, itemType)

                    Toast.makeText(this@ReviewEditActivity, "수정 완료", Toast.LENGTH_SHORT).show()
                    finish()
                }
                setEditMode(false)
            } else {
                setEditMode(true)
            }
        }

        // 후기 상세 조회
        lifecycleScope.launch {
            try {
                val response = api.getReviewDetail(reviewId, itemType)
                val body = response.body()
                if (response.isSuccessful && body != null && body.isSuccess) {
                    val review = body.result

                    // 헤더 영역: 표시용 닉네임 우선 → 없으면 canEdit일 때 내 닉네임
                    if (!displayNickname.isNullOrBlank()) {
                        binding.reviewerNameDetail2.text = displayNickname
                    } else if (canEdit) {
                        binding.reviewerNameDetail2.text = TokenManager.getNickname() ?: "나"
                    }
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

    private fun setMyProfile() {
        // 1) 캐시 우선
        val cachedNick = TokenManager.getNickname()
        binding.reviewerNameDetail2.text = if (!cachedNick.isNullOrBlank()) cachedNick else "나"

        // 2) 최신 프로필 보강 (토큰 있으면)
        val token = TokenManager.getAccessToken()
        if (token.isNullOrEmpty()) return

        lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.memberApi.getProfile()
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val result = resp.body()!!.result
                    val nick = result.nickname ?: "나"
                    binding.reviewerNameDetail2.text = nick
                    TokenManager.saveNickname(nick)

                    val url = result.profileImageUrl
                    val iv = runCatching { binding.root.findViewById<ImageView>(R.id.profileImage2) }.getOrNull()
                    iv?.let {
                        if (!url.isNullOrBlank()) {
                            Glide.with(this@ReviewEditActivity)
                                .load(url)
                                .placeholder(R.drawable.profile_base)
                                .error(R.drawable.profile_base)
                                .circleCrop()
                                .into(it)
                        } else {
                            it.setImageResource(R.drawable.profile_base)
                        }
                    }
                }
            } catch (_: Exception) {
                // 실패 시 캐시 유지
            }
        }
    }

    private fun setEditMode(enabled: Boolean) {
        if (enabled && !canEdit) return
        isEditMode = enabled
        val buttonRes = if (enabled) R.drawable.completebtn_editreview else R.drawable.editbutton
        binding.editButton.setImageResource(buttonRes)

        val editable = enabled && canEdit
        binding.reviewTextDetail2.isEnabled = editable
        binding.reviewTextDetail2.isFocusable = editable
        binding.reviewTextDetail2.isFocusableInTouchMode = editable

        binding.addImageButton.isEnabled = editable
        binding.addImageButton.visibility = if (editable) View.VISIBLE else View.GONE
        binding.ratingBar.setIsIndicator(!editable)

        showAllImages()
    }

    private fun showAllImages() {
        val container = binding.imageContainer
        container.removeAllViews()

        // 추가 버튼
        binding.addImageButton.apply {
            visibility = if (isEditMode && canEdit) View.VISIBLE else View.GONE
            setOnClickListener {
                if (isEditMode && canEdit) pickImagesLauncher.launch("image/*")
            }
        }
        container.addView(binding.addImageButton)

        // 기존 이미지
        existingImageList.forEach { image ->
            val imageLayout = layoutInflater.inflate(R.layout.item_edit_review_image, container, false) as FrameLayout
            val imageView = imageLayout.findViewById<ImageView>(R.id.imageView)
            val removeButton = imageLayout.findViewById<ImageView>(R.id.deleteButton)

            Glide.with(this).load(image.url).into(imageView)

            removeButton.visibility = if (isEditMode && canEdit) View.VISIBLE else View.GONE
            removeButton.setOnClickListener {
                if (!canEdit) return@setOnClickListener
                deletedImageIds.add(image.id)
                existingImageList.remove(image)
                showAllImages()
            }
            container.addView(imageLayout)
        }

        // 새로 추가된 이미지
        selectedImageUris.forEach { uri ->
            val imageLayout = layoutInflater.inflate(R.layout.item_edit_review_image, container, false) as FrameLayout
            val imageView = imageLayout.findViewById<ImageView>(R.id.imageView)
            val removeButton = imageLayout.findViewById<ImageView>(R.id.deleteButton)

            imageView.setImageURI(uri)

            removeButton.visibility = if (isEditMode && canEdit) View.VISIBLE else View.GONE
            removeButton.setOnClickListener {
                if (!canEdit) return@setOnClickListener
                selectedImageUris.remove(uri)
                showAllImages()
            }
            container.addView(imageLayout)
        }
    }

    private suspend fun deleteSelectedImages(reviewId: Long, itemType: String) {
        if (!canEdit || deletedImageIds.isEmpty()) return
        try {
            val request = DeleteReviewImageRequest(imageIds = deletedImageIds)
            val response = api.deleteReviewImages(reviewId, itemType, request)
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Toast.makeText(this, "이미지 삭제 완료", Toast.LENGTH_SHORT).show()
                deletedImageIds.clear()
                refreshReviewDetail(reviewId, itemType) // 최신 상태 반영
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
        if (!canEdit) return
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
        if (!canEdit) return
        val content = binding.reviewTextDetail2.text.toString()
        val rate = binding.ratingBar.rating.toInt()
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
