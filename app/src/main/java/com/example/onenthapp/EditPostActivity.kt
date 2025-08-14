package com.example.onenthapp

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.onenthapp.data.PatchPostPayload
import com.example.onenthapp.databinding.ActivityEditPostsBinding
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.appcompat.app.AlertDialog

class EditPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPostsBinding
    private var postId: Long = -1L
    private var postType: PostType = PostType.UNKNOWN

    // 이미지
    private val maxImages = 5
    private sealed class UiImage {
        data class Existing(val url: String) : UiImage()
        data class New(val uri: Uri) : UiImage()
    }
    private val uiImages = mutableListOf<UiImage>()
    private val http by lazy { OkHttpClient() }

    // 타입
    private enum class PostType { LIFE_TIP, DISCOUNT, RESTAURANT, UNKNOWN }
    private fun parsePostType(raw: String?): PostType = when (raw?.uppercase()) {
        "LIFE_TIP" -> PostType.LIFE_TIP
        "DISCOUNT" -> PostType.DISCOUNT
        "RESTAURANT" -> PostType.RESTAURANT
        else -> PostType.UNKNOWN
    }

    private fun applyUiFor(type: PostType) = with(binding) {
        when (type) {
            PostType.LIFE_TIP -> {
                sectionLink.visibility = View.VISIBLE
                sectionLocation.visibility = View.GONE
            }
            PostType.DISCOUNT, PostType.RESTAURANT -> {
                sectionLink.visibility = View.GONE
                sectionLocation.visibility = View.VISIBLE
            }
            else -> {
                sectionLink.visibility = View.GONE
                sectionLocation.visibility = View.GONE
            }
        }
    }

    // 여러 장 선택
    private val pickImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNullOrEmpty()) return@registerForActivityResult
            val remain = maxImages - uiImages.size
            if (remain <= 0) {
                Toast.makeText(this, "이미지는 최대 $maxImages 장까지 가능합니다.", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            val existingUris = uiImages.filterIsInstance<UiImage.New>().map { it.uri }.toSet()
            val toAdd = uris.filterNot { it in existingUris }.take(remain)
            if (toAdd.isEmpty()) return@registerForActivityResult
            uiImages += toAdd.map { UiImage.New(it) }
            updateImageCount()
            renderThumbnails()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트로 전달된 값
        postId = intent.getLongExtra("postId", -1L)
        postType = parsePostType(intent.getStringExtra("postType"))

        if (postId <= 0L) {
            Toast.makeText(this, "잘못된 게시글입니다.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        setupUi()
        applyUiFor(postType) // 타입 기반으로 링크/장소 섹션 표시
        loadDetail()
    }

    private fun setupUi() = with(binding) {
        ivBack.setOnClickListener { finish() }
        ivEdit.setOnClickListener { submitUpdate() }

        // ✅ 팝업 없이 바로 삭제 실행
        ivDelete.setOnClickListener { doDeletePost() }

        cameraTile.setOnClickListener { /* ... */ }

        updateImageCount()
        renderThumbnails()
    }


//    private fun confirmDelete() {
//        AlertDialog.Builder(this)
//            .setTitle("게시글 삭제")
//            .setMessage("이 게시글을 삭제할까요?")
//            .setPositiveButton("삭제") { _, _ -> doDeletePost() }
//            .setNegativeButton("취소", null)
//            .show()
//    }

    /** 상세 조회 → 제목/내용/기존 이미지(URL) 선반영 */
    private fun loadDetail() {
        lifecycleScope.launch {
            try {
                val token = TokenManager.getAccessToken().orEmpty()
                val response = RetrofitInstance.postApi.getPostDetail("Bearer $token", postId)
                if (!response.isSuccessful) {
                    Toast.makeText(this@EditPostActivity, "로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val body = response.body()
                val d = body?.result
                if (body?.isSuccess != true || d == null) {
                    Toast.makeText(this@EditPostActivity, body?.message ?: "게시글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                binding.etTitle.setText(d.title)
                binding.etContent.setText(d.content)

                // 타입이 인텐트에 없으면 서버 값으로 보정 (있다면 유지)
                if (postType == PostType.UNKNOWN) {
                    postType = parsePostType(d.regionName?.let { "DISCOUNT" } ?: "LIFE_TIP")
                    applyUiFor(postType)
                }

                uiImages.clear()
                (d.imageUrls ?: emptyList()).forEach { url -> uiImages += UiImage.Existing(url) }
                updateImageCount()
                renderThumbnails()

                // 링크/장소 텍스트는 상세 응답 스펙에 없으면 비워둠
                // binding.etLink.setText(d.link ?: "")
                // binding.etLocation.setText(d.address ?: "")

            } catch (e: Exception) {
                Toast.makeText(this@EditPostActivity, "로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 수정(PATCH) — 남아있는 이미지들(기존+신규)을 모두 업로드하여 교체 */
    private fun submitUpdate() {
        val title = binding.etTitle.text?.toString()?.trim().orEmpty()
        val content = binding.etContent.text?.toString()?.trim().orEmpty()

        // 타입별 필드 수집 (서버 규칙: 변경 없으면 "" 빈문자열)
        val link = if (postType == PostType.LIFE_TIP)
            binding.etLink.text?.toString()?.trim().orEmpty()
        else ""

        val address: String
        val placeName: String
        if (postType == PostType.DISCOUNT || postType == PostType.RESTAURANT) {
            address = binding.etLocation.text?.toString()?.trim().orEmpty()
            placeName = address // 장소명을 별도 입력받으면 해당 값으로 교체
        } else {
            address = ""
            placeName = ""
        }

        val payload = PatchPostPayload(
            title = title,
            content = content,
            address = address,
            placeName = placeName,
            link = link,
            tags = emptyList() // 태그 UI 연결 시 채워넣기
        )

        val postPart: RequestBody = com.google.gson.Gson()
            .toJson(payload)
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        binding.ivEdit.isEnabled = false

        lifecycleScope.launch {
            try {
                val token = TokenManager.getAccessToken().orEmpty()

                val parts = withContext(Dispatchers.IO) {
                    uiImages.take(maxImages).mapIndexedNotNull { idx, img ->
                        when (img) {
                            is UiImage.Existing -> urlToPart(img.url, "images", "keep_$idx")
                            is UiImage.New      -> uriToPart(img.uri, "images", "new_$idx")
                        }
                    }
                }

                val resp = RetrofitInstance.postApi.patchPost(
                    token = "Bearer $token",
                    postId = postId,
                    postJson = postPart,
                    images = parts
                )

                if (resp.isSuccess) {
                    Toast.makeText(this@EditPostActivity, "수정되었습니다.", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@EditPostActivity, resp.message ?: "수정 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditPostActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.ivEdit.isEnabled = true
            }
        }
    }

    private fun doDeletePost() {
        val token = TokenManager.getAccessToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 중복 클릭 방지
        binding.ivDelete.isEnabled = false
        binding.ivEdit.isEnabled = false

        lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.postApi.deletePost(
                    bearer = "Bearer $token",
                    postId = postId
                )

                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    Toast.makeText(this@EditPostActivity, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)  // 목록으로 돌아가 새로고침 용
                    finish()
                } else {
                    val msg = resp.body()?.message ?: "삭제 실패 (HTTP ${resp.code()})"
                    Toast.makeText(this@EditPostActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditPostActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.ivDelete.isEnabled = true
                binding.ivEdit.isEnabled = true
            }
        }
    }


    // ---------------- 썸네일 렌더 ----------------

    private fun renderThumbnails() {
        val container = binding.thumbsContainer
        container.removeAllViews()

        uiImages.forEachIndexed { index, img ->
            container.addView(
                createThumbFrame(
                    image = img,
                    onRemove = {
                        uiImages.removeAt(index)
                        updateImageCount()
                        renderThumbnails()
                    }
                )
            )
        }
    }

    private fun createThumbFrame(
        image: UiImage,
        onRemove: () -> Unit
    ): View {
        val size = dp(116)
        val marginStart = dp(8)
        val corner = dp(12) // 모서리 살짝 라운드

        val frame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                setMargins(marginStart, 0, 0, 0)
            }
            background = getDrawable(R.drawable.rectangle_11)
        }

        val iv = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val source: Any = when (image) {
            is UiImage.Existing -> image.url
            is UiImage.New -> image.uri
        }

        Glide.with(this)
            .load(source)
            .transform(CenterCrop(), RoundedCorners(corner))
            .into(iv)

        frame.addView(iv)

        val btnDel = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(dp(22), dp(22)).apply {
                gravity = android.view.Gravity.END or android.view.Gravity.TOP
                setMargins(dp(6), dp(6), dp(6), dp(6))
            }
            setImageResource(R.drawable.btn_delete)
            setPadding(dp(6), dp(6), dp(6), dp(6))
            setOnClickListener { onRemove() }
            bringToFront()
        }
        frame.addView(btnDel)

        return frame
    }

    private fun updateImageCount() {
        binding.tvImageCount.text = "${uiImages.size}/$maxImages"
    }

    private fun dp(value: Int): Int =
        (resources.displayMetrics.density * value).toInt()

    // --------------- URL/URI → Multipart ---------------

    private fun urlToPart(url: String, partName: String, fallbackName: String): MultipartBody.Part? {
        return try {
            val req = Request.Builder().url(url).build()
            val res = http.newCall(req).execute()
            if (!res.isSuccessful) return null
            val bytes = res.body?.bytes() ?: return null
            val mime = res.body?.contentType()?.toString() ?: guessMimeFromUrl(url)
            val fileName = url.substringAfterLast('/').ifBlank { "$fallbackName.jpg" }
            val rb = bytes.toRequestBody(mime.toMediaType())
            MultipartBody.Part.createFormData(partName, fileName, rb)
        } catch (_: Throwable) { null }
    }

    private fun uriToPart(uri: Uri, partName: String, fallbackName: String): MultipartBody.Part? {
        return try {
            val mime = contentResolver.getType(uri) ?: "image/jpeg"
            val fileName = queryDisplayName(uri) ?: "$fallbackName.jpg"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val rb = bytes.toRequestBody(mime.toMediaType())
            MultipartBody.Part.createFormData(partName, fileName, rb)
        } catch (_: Throwable) { null }
    }

    private fun queryDisplayName(uri: Uri): String? = try {
        contentResolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
        }
    } catch (_: Throwable) { null }

    private fun guessMimeFromUrl(url: String): String = when {
        url.endsWith(".png", true)  -> "image/png"
        url.endsWith(".webp", true) -> "image/webp"
        else                        -> "image/jpeg"
    }
}
