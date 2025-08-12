package com.example.onenthapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.onenthapp.data.post.PostPayload
import com.example.onenthapp.data.post.buildImageParts
import com.example.onenthapp.data.post.buildPostJsonPart
import com.example.onenthapp.databinding.ActivityLifetipsWriteBinding
import com.example.onenthapp.util.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class CreateLifePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLifetipsWriteBinding

    // 선택 이미지(최대 10장)
    private val pickedUris = mutableListOf<Uri>()
    private val maxImages = 5

    // 갤러리에서 여러 장 선택
    private val pickImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (!uris.isNullOrEmpty()) {
                // 이미 선택된 것 유지 + 새로 선택한 것 추가 (중복 제거, 남은 슬롯만)
                val remain = maxImages - pickedUris.size
                if (remain <= 0) {
                    Toast.makeText(this, "최대 ${maxImages}장까지 가능합니다.", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                val toAdd = uris.filterNot { it in pickedUris }.take(remain)
                if (toAdd.isEmpty()) {
                    Toast.makeText(this, "추가할 수 있는 이미지가 없습니다.", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                pickedUris.addAll(toAdd)
            }
            updateImageCount()
            renderThumbnails()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLifetipsWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // postType: 기본 LIFE_TIP (필요시 외부에서 "DISCOUNT"/"RESTAURANT"로 넘겨도 됨)
        val postType = (intent.getStringExtra("postType") ?: "LIFE_TIP").uppercase()

        // 상단바: 뒤로가기(이미지뷰) + 우측 "올리기"(TextView)
        binding.ivBack.setOnClickListener { finish() }
        binding.ivNotification.setOnClickListener { submit(postType) }

        // 카메라 타일 클릭 → 이미지 선택
        binding.cameraTile.setOnClickListener {
            pickImagesLauncher.launch("image/*")
        }

        // 초기 렌더
        updateImageCount()
        renderThumbnails()
    }

    /** 상단의 "1/10" 같은 카운트 UI 갱신 */
    private fun updateImageCount() {
        binding.tvImageCount.text = "${pickedUris.size}/$maxImages"
    }

    /** 오른쪽 썸네일 리스트 그리기 */
    private fun renderThumbnails() {
        val container = binding.thumbsContainer
        container.removeAllViews()

        pickedUris.forEachIndexed { index, uri ->
            container.addView(
                createThumbFrame(
                    uri = uri,
                    index = index,
                    onRemove = {
                        pickedUris.removeAt(index)
                        updateImageCount()
                        renderThumbnails()
                    }
                )
            )
        }
    }


    /** 개별 썸네일 셀(프레임) 생성 */
    private fun createThumbFrame(
        uri: Uri,
        index: Int,
        onRemove: () -> Unit
    ): View {
        // 크기/마진(dp → px)
        val size = dp(116)
        val marginStart = dp(8)

        val frame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                setMargins(marginStart, 0, 0, 0)
            }
            background = getDrawable(R.drawable.rectangle_11) // 기존 박스 배경
        }

        // 썸네일 이미지
        val iv = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            clipToOutline = true
        }
        Glide.with(this).load(uri).into(iv)
        frame.addView(iv)

        // 삭제 버튼
        val btnDel = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(dp(22), dp(22)).apply {
                // 오른쪽 위
                gravity = android.view.Gravity.END or android.view.Gravity.TOP
                val pad = dp(10)
                setPadding(pad)
                setMargins(pad, pad, pad, pad)
            }
            setImageResource(R.drawable.btn_delete)
            setOnClickListener { onRemove() }
        }
        frame.addView(btnDel)

        return frame
    }

    /** 글 등록 */
    private fun submit(postType: String) {
        val title = binding.etTitle.text?.toString()?.trim().orEmpty()
        val content = binding.etContent.text?.toString()?.trim().orEmpty()
        val link = binding.etLink.text?.toString()?.trim().orEmpty()
        val tagsInput = binding.etTags.text?.toString()?.trim().orEmpty()
        val tags = if (tagsInput.isBlank()) emptyList() else tagsInput.split(",").map { it.trim() }

        if (title.isBlank() || content.isBlank()) {
            Toast.makeText(this, "제목과 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val payload = when (postType) {
            "LIFE_TIP" -> PostPayload(title = title, content = content, link = link.ifBlank { null }, tags = tags)
            "DISCOUNT", "RESTAURANT" -> PostPayload(title = title, content = content, address = null, placeName = null, tags = tags)
            else -> {
                Toast.makeText(this, "지원하지 않는 postType 입니다.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val token = TokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val postPart = buildPostJsonPart(payload) // ✅ text/plain
        val imageParts = applicationContext.buildImageParts(pickedUris.take(maxImages))
        val imagesArg = if (imageParts.isEmpty()) null else imageParts

        setLoading(true)

        lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.postApi.createPost(
                    bearer = "Bearer $token",           // ✅ 헤더 전달
                    postType = postType.uppercase(),
                    postJson = postPart,
                    images = imagesArg
                )
                setLoading(false)

                if (resp.isSuccessful) {
                    val body = resp.body()
                    if (body?.isSuccess == true) {
                        Toast.makeText(this@CreateLifePostActivity, "등록 완료 (id=${body.result?.postId})", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@CreateLifePostActivity, "등록 실패: [${body?.code}] ${body?.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val err = resp.errorBody()?.string()
                    Toast.makeText(this@CreateLifePostActivity, "HTTP ${resp.code()} 실패: $err", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(this@CreateLifePostActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        // 레이아웃에 ProgressBar(id=progress) 하나 추가해두면 좋아요.
        // 일단 없으면 "올리기" 버튼 비활성화만 처리
        binding.ivNotification.isEnabled = !loading
        // binding.progress.isVisible = loading
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    // post JSON을 text/plain 으로 보내기 (서버가 문자열로 받는 경우 호환성↑)
    private fun buildPostJsonPart(payload: PostPayload): RequestBody {
        val json = Gson().toJson(payload)
        return json.toRequestBody("application/json; charset=utf-8".toMediaType())
    }

    // 이미지 멀티파트 변환 (키 이름은 "images")
    fun Context.buildImageParts(uris: List<Uri>): List<MultipartBody.Part> {
        val parts = mutableListOf<MultipartBody.Part>()
        for ((i, uri) in uris.withIndex()) {
            val mime = contentResolver.getType(uri) ?: "image/*"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: continue
            val rb = bytes.toRequestBody(mime.toMediaTypeOrNull())
            // 파일명은 대충 index 기반으로
            val fileName = "image_${i}.jpg"
            parts += MultipartBody.Part.createFormData("images", fileName, rb)
        }
        return parts
    }
}

