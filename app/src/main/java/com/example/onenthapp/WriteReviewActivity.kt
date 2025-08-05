package com.example.onenthapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.onenthapp.data.ReviewApi
import com.example.onenthapp.data.ReviewBody
import com.example.onenthapp.data.ReviewResponse
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream

class WriteReviewActivity : AppCompatActivity() {

    private lateinit var apiService: ReviewApi
    private lateinit var imageContainer: LinearLayout
    private lateinit var btnAddImage: ImageView

    private val imageUris = mutableListOf<Uri>()

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (imageUris.size >= 3) {
                Toast.makeText(this, "사진은 최대 3장까지 첨부할 수 있어요.", Toast.LENGTH_SHORT).show()
                return@let
            }
            imageUris.add(it)
            addImagePreview(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_review)

        apiService = RetrofitInstance.reviewApi

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val reviewEditText = findViewById<EditText>(R.id.reviewEditText)
        val submitButton = findViewById<ImageButton>(R.id.btnSubmit)
        imageContainer = findViewById(R.id.imageContainer)
        btnAddImage = findViewById(R.id.btnAddImage)

        val purchaseItemId = intent.getLongExtra("purchaseItemId", -1L)

        btnAddImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        submitButton.setOnClickListener {
            val content = reviewEditText.text.toString()
            val rate = ratingBar.rating

            if (purchaseItemId == -1L) {
                Toast.makeText(this, "물품 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (rate == 0f) {
                Toast.makeText(this, "별점을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitReview(purchaseItemId, content, rate, imageUris)
        }
    }

    private fun addImagePreview(uri: Uri) {
        val wrapper = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 16, 0)
        }

        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(150, 150)
            setImageURI(uri)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundResource(R.color.main_gray)
        }

        val closeBtn = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(30, 30)
            setImageResource(R.drawable.proicons_cancel)
            setOnClickListener {
                imageContainer.removeView(wrapper)
                imageUris.remove(uri)
            }
        }

        wrapper.addView(imageView)
        wrapper.addView(closeBtn)
        imageContainer.addView(wrapper)
    }

    fun submitReview(
        purchaseItemId: Long,
        content: String,
        rate: Float,
        imageUris: List<Uri>?
    ) {
        val reviewJson = Gson().toJson(ReviewBody(content, rate))
        val reviewRequestBody = reviewJson.toRequestBody("application/json".toMediaType())

        val imageParts = imageUris?.mapNotNull { uri ->
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@mapNotNull null
                val requestFile = bytes.toRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData(
                    "images",
                    "image_${System.currentTimeMillis()}.jpg",
                    requestFile
                )
            } catch (e: Exception) {
                Log.e("REVIEW", "이미지 변환 실패: ${e.localizedMessage}")
                null
            }
        }

        val call = apiService.submitPurchaseReview(purchaseItemId, reviewRequestBody, imageParts)

        call.enqueue(object : Callback<ReviewResponse> {
            override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("REVIEW", "리뷰 전송 성공: ${response.body()?.result?.puchaseReviewId}")
                    Toast.makeText(this@WriteReviewActivity, "후기 작성이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e("REVIEW", "리뷰 실패: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@WriteReviewActivity, "후기 작성 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                Log.e("REVIEW", "서버 통신 실패", t)
                Toast.makeText(this@WriteReviewActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }
}