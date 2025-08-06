package com.example.onenthapp

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.onenthapp.data.ReviewDetailResult
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
    private val existingImageUrls = mutableListOf<String>()

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

        // Toolbar 뒤로가기
        binding.topAppBar.setNavigationOnClickListener { finish() }

        // 이미지 추가 버튼 동작
        binding.addImageButton.setOnClickListener {
            pickImagesLauncher.launch("image/*")
        }

        // 리뷰 수정 완료 버튼
        binding.editButton.setOnClickListener {
            uploadReviewImages(selectedImageUris)
        }

        // 기존 리뷰 불러오기
        lifecycleScope.launch {
            try {
                val response = api.getReviewDetail(reviewId, itemType)
                val body = response.body()
                if (response.isSuccessful && body != null && body.isSuccess) {
                    val review = body.result

                    binding.reviewerNameDetail2.text = "나"
                    binding.productNameText2.text = "상품 ID: ${review.itemId}"
                    val stars = "★★★★★".substring(0, review.rate) + "☆☆☆☆☆".substring(0, 5 - review.rate)
                    binding.starRatingDetail2.text = stars
                    binding.reviewTextDetail2.setText(review.content)

                    existingImageUrls.clear()
                    existingImageUrls.addAll(review.reviewImageList)

                    showAllImages()

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

    private fun showAllImages() {
        val container = binding.imageContainer
        container.removeAllViews()

        // 1. 항상 맨 앞에 이미지 추가 버튼
        container.addView(binding.addImageButton)

        // 2. 기존 이미지 표시
        existingImageUrls.forEach { url ->
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(98.dpToPx(), 93.dpToPx()).apply {
                    marginEnd = 20.dpToPx()
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundResource(R.color.image_placeholder)
            }
            Glide.with(this).load(url).into(imageView)
            container.addView(imageView)
        }

        // 3. 추가된 이미지 표시
        selectedImageUris.forEach { uri ->
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(98.dpToPx(), 93.dpToPx()).apply {
                    marginEnd = 20.dpToPx()
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
                setBackgroundResource(R.color.image_placeholder)
            }
            container.addView(imageView)
        }
    }

    private fun uploadReviewImages(imageUris: List<Uri>) {
        val reviewId = intent.getLongExtra("reviewId", -1)
        val itemType = intent.getStringExtra("itemType") ?: ""

        if (imageUris.isEmpty()) {
            Toast.makeText(this, "선택된 이미지가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val imageParts = prepareImageParts(imageUris)

        lifecycleScope.launch {
            try {
                val response = api.uploadReviewImages(
                    reviewId = reviewId,
                    itemType = itemType,
                    images = imageParts
                )
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(this@ReviewEditActivity, "이미지 추가 완료", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ReviewEditActivity, "업로드 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ReviewEditActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

//    private fun prepareImageParts(imageUris: List<Uri>): List<MultipartBody.Part> {
//        val parts = mutableListOf<MultipartBody.Part>()
//        imageUris.forEach { uri ->
//            val file = File(getRealPathFromUri(uri) ?: return@forEach)
//            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
//            val part = MultipartBody.Part.createFormData("images", file.name, requestFile)
//            parts.add(part)
//        }
//        return parts
//    }
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


    private fun getRealPathFromUri(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            it.moveToFirst()
            val index = it.getColumnIndex(MediaStore.Images.Media.DATA)
            if (index != -1) it.getString(index) else null
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
