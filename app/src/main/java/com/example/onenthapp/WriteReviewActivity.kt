package com.example.onenthapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
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

        // ← 툴바 뒤로가기(있으면)
        findViewById<com.google.android.material.appbar.MaterialToolbar?>(R.id.topAppBar)?.let { tb ->
            // 만약 커스텀 툴바를 액션바로 쓰고 있다면 주석 해제
            // setSupportActionBar(tb)
            tb.setNavigationOnClickListener { finish() }
        }
        // ← 액션바 사용하는 레이아웃일 경우 홈버튼으로 뒤로가기 표시
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        apiService = RetrofitInstance.reviewApi

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val reviewEditText = findViewById<EditText>(R.id.reviewEditText)
        val submitButton = findViewById<ImageButton>(R.id.btnSubmit)
        imageContainer = findViewById(R.id.imageContainer)
        btnAddImage = findViewById(R.id.btnAddImage)

        val purchaseItemId = intent.getLongExtra("purchaseItemId", -1L)
        val sharingItemId = intent.getLongExtra("sharingItemId", -1L)

        btnAddImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        submitButton.setOnClickListener {
            val content = reviewEditText.text.toString()
            val rate = ratingBar.rating

            if (rate == 0.0f) {
                Toast.makeText(this, "별점을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (purchaseItemId != -1L) {
                submitPurchaseReview(purchaseItemId, content, rate, imageUris)
            } else if (sharingItemId != -1L) {
                submitSharingReview(sharingItemId, content, rate, imageUris)
            } else {
                Toast.makeText(this, "물품 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // 액션바 홈(뒤로가기) 아이콘 클릭 시
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun Int.dp() = (this * resources.displayMetrics.density).toInt()

    private fun addImagePreview(uri: Uri) {
        val thumb = 57.dp()          // 썸네일 한 칸 크기 (btnAddImage와 맞춤)
        val gap   = 8.dp()
        val xSize = 18.dp()
        val xPad  = 2.dp()
        val inset = 4.dp()           // 모서리에서 조금 띄우기

        // 1) 썸네일 한 칸(정사각형)
        val wrapper = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(thumb, thumb).apply {
                setMargins(0, 0, gap, 0)
            }
        }

        // 2) 이미지
        val imageView = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            // 모서리 둥글게 하고 싶으면 썸네일 배경(shape) 사용
            // background = ContextCompat.getDrawable(this@WriteReviewActivity, R.drawable.bg_thumb_8dp)
            // clipToOutline = true
        }
        imageView.setImageURI(uri) // Glide 쓰면 centerCrop().into(imageView)

        // 3) 닫기 버튼 (오버레이)
        val closeBtn = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(xSize, xSize, Gravity.TOP or Gravity.END).apply {
                setMargins(0, inset, inset, 0)
            }
            setImageResource(R.drawable.proicons_cancel) // 있는 X 아이콘 그대로 사용
//            setPadding(xPad)
            // 배경 원(흰색 반투명)을 쓰고 싶으면 지정
            // background = ContextCompat.getDrawable(this@WriteReviewActivity, R.drawable.bg_close_circle)
            contentDescription = "이미지 삭제"
            setOnClickListener {
                imageContainer.removeView(wrapper)
                imageUris.remove(uri)
            }
        }

        wrapper.addView(imageView)
        wrapper.addView(closeBtn)

        // add 버튼 다음에 붙이고 싶으면 index를 지정해도 됨
        // val insertIndex = imageContainer.indexOfChild(btnAddImage) + 1
        // imageContainer.addView(wrapper, insertIndex)

        imageContainer.addView(wrapper)
    }


    fun submitPurchaseReview(
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
                    Log.d("REVIEW", "구매 후기 전송 성공: ${response.body()?.result?.puchaseReviewId}")
                    Toast.makeText(this@WriteReviewActivity, "후기 작성이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e("REVIEW", "구매 후기 실패: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@WriteReviewActivity, "후기 작성 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                Log.e("REVIEW", "구매 후기 서버 통신 실패", t)
                Toast.makeText(this@WriteReviewActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }


    fun submitSharingReview(
        sharingItemId: Long,
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

        val call = apiService.submitSharingReview(sharingItemId, reviewRequestBody, imageParts)

        call.enqueue(object : Callback<ReviewResponse> {
            override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("REVIEW", "공유 후기 전송 성공: ${response.body()?.result?.sharingReviewId}")
                    Toast.makeText(this@WriteReviewActivity, "후기 작성이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e("REVIEW", "공유 후기 실패: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@WriteReviewActivity, "후기 작성 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                Log.e("REVIEW", "공유 후기 서버 통신 실패", t)
                Toast.makeText(this@WriteReviewActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }

}