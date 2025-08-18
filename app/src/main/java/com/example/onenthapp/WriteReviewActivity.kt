package com.example.onenthapp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.onenthapp.data.ReviewApi
import com.example.onenthapp.data.ReviewBody
import com.example.onenthapp.data.ReviewResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.util.Locale

class WriteReviewActivity : AppCompatActivity() {

    private lateinit var apiService: ReviewApi
    private lateinit var imageContainer: LinearLayout
    private lateinit var btnAddImage: ImageView
    private lateinit var submitButton: ImageButton
    private lateinit var ratingBar: RatingBar
    private lateinit var reviewEditText: EditText
    private lateinit var tvItemName: TextView
    private lateinit var ivItemImage: ImageView

    private val imageUris = mutableListOf<Uri>()

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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

        // 툴바 뒤로가기
        findViewById<com.google.android.material.appbar.MaterialToolbar?>(R.id.topAppBar)?.let { tb ->
            tb.setNavigationOnClickListener { finish() }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 뷰 바인딩
        apiService = RetrofitInstance.reviewApi
        ratingBar = findViewById(R.id.ratingBar)
        reviewEditText = findViewById(R.id.reviewEditText)
        submitButton = findViewById(R.id.btnSubmit)
        imageContainer = findViewById(R.id.imageContainer)
        btnAddImage = findViewById(R.id.btnAddImage)
        tvItemName = findViewById(R.id.tvItemName)
        ivItemImage = findViewById(R.id.ivItemImage)

        // 화면 표시 데이터
        val itemName = intent.getStringExtra("itemName").orEmpty()
        val itemImageUrl = intent.getStringExtra("itemImageUrl")

        tvItemName.text = if (itemName.isNotBlank()) itemName else "상품명 미상"
        if (!itemImageUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(itemImageUrl)
                .centerCrop()
                .placeholder(R.drawable.rectangle_51)
                .error(R.drawable.rectangle_51)
                .into(ivItemImage)
        } else {
            ivItemImage.setImageResource(R.drawable.rectangle_51)
        }

        // 아이템 ID 복구(안전하게 Long/Int 모두 처리)
        var purchaseItemId = getLongOrInt("purchaseItemId")
        var sharingItemId = getLongOrInt("sharingItemId")

        if (purchaseItemId == null && sharingItemId == null) {
            val type = intent.getStringExtra("itemType")?.uppercase(Locale.ROOT)
            val uniId = getLongOrInt("itemId")
            if (uniId != null) {
                when (type) {
                    "PURCHASE" -> purchaseItemId = uniId
                    "SHARING"  -> sharingItemId  = uniId
                }
            }
        }

        Log.d("WriteReview", "purchaseId=$purchaseItemId, sharingId=$sharingItemId")

        // 이미지 추가
        btnAddImage.setOnClickListener { imagePickerLauncher.launch("image/*") }

        // 제출
        submitButton.setOnClickListener {
            val content = reviewEditText.text.toString().trim()
            val rate = ratingBar.rating

            // 스펙: 0.5 ~ 5.0
            if (rate < 0.5f) {
                Toast.makeText(this, "별점을 0.5 이상 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 중복 클릭 방지
            submitButton.isEnabled = false

            when {
                purchaseItemId != null -> submitPurchaseReview(purchaseItemId!!, content, rate, imageUris)
                sharingItemId  != null -> submitSharingReview(sharingItemId!!,  content, rate, imageUris)
                else -> {
                    Toast.makeText(this, "물품 정보가 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    submitButton.isEnabled = true
                }
            }
        }
    }

    // 액션바 홈(뒤로가기)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish(); true
        } else super.onOptionsItemSelected(item)
    }

    // ---------- 유틸 ----------

    private fun Int.dp() = (this * resources.displayMetrics.density).toInt()

    private fun addImagePreview(uri: Uri) {
        val thumb = 57.dp()
        val gap   = 8.dp()
        val xSize = 18.dp()
        val inset = 4.dp()

        val wrapper = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(thumb, thumb).apply { setMargins(0, 0, gap, 0) }
        }

        val imageView = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        imageView.setImageURI(uri)

        val closeBtn = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(xSize, xSize, Gravity.TOP or Gravity.END).apply {
                setMargins(0, inset, inset, 0)
            }
            setImageResource(R.drawable.proicons_cancel)
            contentDescription = "이미지 삭제"
            setOnClickListener {
                imageContainer.removeView(wrapper)
                imageUris.remove(uri)
            }
        }

        wrapper.addView(imageView)
        wrapper.addView(closeBtn)
        imageContainer.addView(wrapper)
    }

    /**
     * 인텐트에 Int 또는 Long 타입으로 들어온 값을 모두 안전하게 Long?으로 꺼내기
     */
    private fun getLongOrInt(key: String): Long? {
        if (!intent.hasExtra(key)) return null
        val any = intent.extras?.get(key) ?: return null
        return when (any) {
            is Long -> any
            is Int  -> any.toLong()
            else    -> null
        }
    }

    // ---------- 업로드 ----------

    private fun submitPurchaseReview(
        purchaseItemId: Long,
        content: String,
        rate: Float,
        imageUris: List<Uri>?
    ) {
        val reviewJson = Gson().toJson(ReviewBody(content, rate))
        val reviewRequestBody = reviewJson.toRequestBody("application/json".toMediaType())
        val imageParts = buildImageParts(imageUris)

        val call = apiService.submitPurchaseReview(purchaseItemId, reviewRequestBody, imageParts)
        call.enqueue(object : Callback<ReviewResponse> {
            override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                submitButton.isEnabled = true
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(this@WriteReviewActivity, "후기 작성이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@WriteReviewActivity, "후기 작성 실패", Toast.LENGTH_SHORT).show()
                    Log.e("REVIEW", "구매 후기 실패: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                submitButton.isEnabled = true
                Toast.makeText(this@WriteReviewActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                Log.e("REVIEW", "구매 후기 서버 통신 실패", t)
            }
        })
    }

    private fun submitSharingReview(
        sharingItemId: Long,
        content: String,
        rate: Float,
        imageUris: List<Uri>?
    ) {
        val reviewJson = Gson().toJson(ReviewBody(content, rate))
        val reviewRequestBody = reviewJson.toRequestBody("application/json".toMediaType())
        val imageParts = buildImageParts(imageUris)

        val call = apiService.submitSharingReview(sharingItemId, reviewRequestBody, imageParts)
        call.enqueue(object : Callback<ReviewResponse> {
            override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                submitButton.isEnabled = true
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(this@WriteReviewActivity, "후기 작성이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@WriteReviewActivity, "후기 작성 실패", Toast.LENGTH_SHORT).show()
                    Log.e("REVIEW", "공유 후기 실패: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                submitButton.isEnabled = true
                Toast.makeText(this@WriteReviewActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                Log.e("REVIEW", "공유 후기 서버 통신 실패", t)
            }
        })
    }

    private fun buildImageParts(imageUris: List<Uri>?): List<MultipartBody.Part>? {
        return imageUris?.mapNotNull { uri ->
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
    }
}
