package com.example.onenthapp

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.onenthapp.data.PatchPostPayload
import com.example.onenthapp.databinding.ActivityEditPostsBinding
import com.example.onenthapp.util.TokenManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class EditPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPostsBinding
    private var postId: Long = -1L
    private var postType: PostType = PostType.UNKNOWN

    // 이미지
    private val maxImages: Int = 5
    private sealed class UiImage {
        data class Existing(val url: String) : UiImage()
        data class New(val uri: Uri) : UiImage()
    }
    private val uiImages: MutableList<UiImage> = mutableListOf()
    private val http: OkHttpClient by lazy { OkHttpClient() }

    // 태그
    private val tagList: MutableList<String> = mutableListOf()
    private val maxTags: Int = 5

    // 링크/장소 값(칩 1개)
    private var linkValue: String? = null
    private var locationValue: String? = null

    // 타입
    private enum class PostType { LIFE_TIP, DISCOUNT, RESTAURANT, UNKNOWN }
    private fun parsePostType(raw: String?): PostType = when (raw?.uppercase()) {
        "LIFE_TIP" -> PostType.LIFE_TIP
        "DISCOUNT" -> PostType.DISCOUNT
        "RESTAURANT" -> PostType.RESTAURANT
        else -> PostType.UNKNOWN
    }

    private fun applyUiFor(type: PostType): Unit = with(binding) {
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
        // 섹션 전환 시 칩도 다시 그려주기
        renderMetaUi()
    }

    // 여러 장 선택
    private val pickImagesLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            if (uris.isEmpty()) return@registerForActivityResult
            val remain: Int = maxImages - uiImages.size
            if (remain <= 0) {
                Toast.makeText(this, "이미지는 최대 $maxImages 장까지 가능합니다.", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            val existingUris: Set<Uri> = uiImages.filterIsInstance<UiImage.New>().map { it.uri }.toSet()
            val toAdd: List<Uri> = uris.filterNot { it in existingUris }.take(remain)
            if (toAdd.isEmpty()) return@registerForActivityResult
            uiImages += toAdd.map { UiImage.New(it) }
            updateImageCount()
            renderThumbnails()
        }

    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getLongExtra("postId", -1L)
        postType = parsePostType(intent.getStringExtra("postType"))
        if (postId <= 0L) {
            Toast.makeText(this, "잘못된 게시글입니다.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        setupUi()
        applyUiFor(postType)
        loadDetail()
    }

    private fun setupUi(): Unit = with(binding) {
        ivBack.setOnClickListener { finish() }
        ivEdit.setOnClickListener { submitUpdate() }
        ivDelete.setOnClickListener { doDeletePost() }

        cameraTile.setOnClickListener {
            val remain: Int = maxImages - uiImages.size
            if (remain <= 0) {
                Toast.makeText(this@EditPostActivity, "이미지는 최대 $maxImages 장까지 가능합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            pickImagesLauncher.launch("image/*")
        }

        // 태그
        setupTagInput()

        // 링크/장소 입력 (엔터로 확정 → 칩으로)
        setupLinkInput()
        setupLocationInput()

        updateImageCount()
        renderThumbnails()
    }

    // ---------------- 태그 칩 ----------------

    private fun setupTagInput(): Unit = with(binding) {
        etTags.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str: String = s?.toString().orEmpty()
                if (str.endsWith(" ") || str.endsWith(",")) addTagFromInput()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        etTags.clearOnEnter { addTagFromInput() }
        etTags.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) addTagFromInput() }
    }

    private fun addTagFromInput(): Unit {
        val raw: String = binding.etTags.text?.toString()?.trim()?.removeSuffix(",").orEmpty()
        val clean: String = raw.removePrefix("#").trim()
        if (clean.isBlank()) { binding.etTags.text?.clear(); return }

        if (tagList.size >= maxTags) {
            Toast.makeText(this, "태그는 최대 ${maxTags}개까지 가능합니다.", Toast.LENGTH_SHORT).show()
            binding.etTags.text?.clear(); return
        }
        if (tagList.any { it.equals(clean, ignoreCase = true) }) {
            binding.etTags.text?.clear(); return
        }

        tagList.add(clean)
        addTagChip(clean)
        binding.etTags.text?.clear()
    }

    private fun addTagChip(tag: String): Unit {
        val chip: Chip = Chip(this).apply {
            text = "# $tag"
            isCloseIconVisible = true
            chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#E7F6ED"))
            setTextColor(ContextCompat.getColor(this@EditPostActivity, R.color.main_green))
            closeIconTint = ColorStateList.valueOf(ContextCompat.getColor(this@EditPostActivity, R.color.main_green))
            chipCornerRadius = 20f * resources.displayMetrics.density
            setEnsureMinTouchTargetSize(false)
            setOnCloseIconClickListener {
                tagList.remove(tag)
                binding.chipGroupTags.removeView(this)
            }
        }
        binding.chipGroupTags.addView(chip)
    }

    // ---------------- 링크/장소 (칩그룹 분리) ----------------

    private fun setupLinkInput(): Unit = with(binding) {
        etLink.clearOnEnter { confirmLinkFromInput() }
        etLink.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) confirmLinkFromInput() }
    }

    private fun setupLocationInput(): Unit = with(binding) {
        etLocation.clearOnEnter { confirmLocationFromInput() }
        etLocation.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) confirmLocationFromInput() }
    }

    private fun confirmLinkFromInput(): Unit {
        if (postType != PostType.LIFE_TIP) return
        if (linkValue != null) { binding.etLink.text?.clear(); return }
        val value: String = binding.etLink.text?.toString()?.trim().orEmpty()
        if (value.isBlank()) return
        linkValue = value
        binding.etLink.text?.clear()
        renderMetaUi()
    }

    private fun confirmLocationFromInput(): Unit {
        if (postType != PostType.DISCOUNT && postType != PostType.RESTAURANT) return
        if (locationValue != null) { binding.etLocation.text?.clear(); return }
        val value: String = binding.etLocation.text?.toString()?.trim().orEmpty()
        if (value.isBlank()) return
        locationValue = value
        binding.etLocation.text?.clear()
        renderMetaUi()
    }

    /** 타입별 칩 1개만 표시 */
    private fun renderMetaUi(): Unit = with(binding) {
        chipGroupLink.removeAllViews()
        chipGroupLocation.removeAllViews()

        when (postType) {
            PostType.LIFE_TIP -> {
                val v: String = linkValue?.trim().orEmpty()
                if (v.isNotEmpty()) {
                    val onClose: () -> Unit = {
                        linkValue = null
                        renderMetaUi() // 삭제 후 리렌더
                        etLink.requestFocus()
                    }
                    chipGroupLink.addView(makeSingleChip(v, onClose))
                }
                etLink.visibility = if (linkValue == null) View.VISIBLE else View.GONE
                etLocation.visibility = View.GONE
            }
            PostType.DISCOUNT, PostType.RESTAURANT -> {
                val v: String = locationValue?.trim().orEmpty()
                if (v.isNotEmpty()) {
                    val onClose: () -> Unit = {
                        locationValue = null
                        renderMetaUi() // 삭제 후 리렌더
                        etLocation.requestFocus()
                    }
                    chipGroupLocation.addView(makeSingleChip(v, onClose))
                }
                etLocation.visibility = if (locationValue == null) View.VISIBLE else View.GONE
                etLink.visibility = View.GONE
            }
            else -> {
                etLink.visibility = View.GONE
                etLocation.visibility = View.GONE
            }
        }
    }

    private fun makeSingleChip(textValue: String, onClose: () -> Unit): Chip =
        Chip(this).apply {
            text = textValue
            isCloseIconVisible = true
            chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#E7F6ED"))
            setTextColor(ContextCompat.getColor(this@EditPostActivity, R.color.main_green))
            closeIconTint = ColorStateList.valueOf(ContextCompat.getColor(this@EditPostActivity, R.color.main_green))
            chipCornerRadius = 20f * resources.displayMetrics.density
            setEnsureMinTouchTargetSize(false)
            setOnCloseIconClickListener {
                onClose()
                // 안전망: 혹시 리렌더 전까지 잔상 방지
                (parent as? ChipGroup)?.removeView(this)
            }
        }

    // ---------------- 상세 로드/제출/삭제 ----------------

    private fun loadDetail(): Unit {
        lifecycleScope.launch {
            try {
                val token: String = TokenManager.getAccessToken().orEmpty()
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

                val hasLink: Boolean = !d.link.isNullOrBlank()
                val hasPlace: Boolean = !d.address.isNullOrBlank() || !d.placeName.isNullOrBlank()
                if (postType == PostType.UNKNOWN) {
                    postType = when {
                        hasLink -> PostType.LIFE_TIP
                        hasPlace -> PostType.DISCOUNT
                        else -> PostType.UNKNOWN
                    }
                }
                applyUiFor(postType)

                // 칩 값 세팅
                linkValue = if (postType == PostType.LIFE_TIP)
                    d.link?.trim()?.takeIf { it.isNotBlank() }
                else null
                locationValue = if (postType == PostType.DISCOUNT || postType == PostType.RESTAURANT)
                    (d.address?.trim()?.takeIf { it.isNotBlank() }
                        ?: d.placeName?.trim()?.takeIf { it.isNotBlank() })
                else null

                // 입력창 텍스트 미러링(선택 사항)
                if (postType == PostType.LIFE_TIP) binding.etLink.setText(linkValue.orEmpty())
                if (postType == PostType.DISCOUNT || postType == PostType.RESTAURANT) binding.etLocation.setText(locationValue.orEmpty())
                renderMetaUi()

                // 태그
                val tags: List<String> = d.tags.orEmpty()
                    .map { it.removePrefix("#").trim() }
                    .filter { it.isNotBlank() }
                    .take(maxTags)

                tagList.clear()
                binding.chipGroupTags.removeAllViews()
                tags.forEach { t -> tagList.add(t); addTagChip(t) }
                binding.etTags.setText("")

                // 이미지
                uiImages.clear()
                (d.imageUrls ?: emptyList()).forEach { url -> uiImages += UiImage.Existing(url) }
                updateImageCount()
                renderThumbnails()

            } catch (e: Exception) {
                Toast.makeText(this@EditPostActivity, "로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitUpdate(): Unit {
        val title: String = binding.etTitle.text?.toString()?.trim().orEmpty()
        val content: String = binding.etContent.text?.toString()?.trim().orEmpty()

        val finalLink: String = linkValue ?: binding.etLink.text?.toString()?.trim().orEmpty()
        val finalLocation: String = locationValue ?: binding.etLocation.text?.toString()?.trim().orEmpty()

        val link: String = if (postType == PostType.LIFE_TIP) finalLink else ""

        // Pair 디스트럭처링 대신 명시 타입으로 분리
        val address: String
        val placeName: String
        if (postType == PostType.DISCOUNT || postType == PostType.RESTAURANT) {
            address = finalLocation
            placeName = finalLocation
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
            tags = tagList.toList()
        )

        val postPart: RequestBody = com.google.gson.Gson()
            .toJson(payload)
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        binding.ivEdit.isEnabled = false

        lifecycleScope.launch {
            try {
                val token: String = TokenManager.getAccessToken().orEmpty()

                val parts: List<MultipartBody.Part> = withContext(Dispatchers.IO) {
                    uiImages.take(maxImages).mapIndexedNotNull { idx, img ->
                        when (img) {
                            is UiImage.Existing -> urlToPart(img.url, "images", "keep_$idx")
                            is UiImage.New -> uriToPart(img.uri, "images", "new_$idx")
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

    private fun doDeletePost(): Unit {
        val token: String? = TokenManager.getAccessToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
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
                    setResult(Activity.RESULT_OK)
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

    private fun renderThumbnails(): Unit {
        val container: LinearLayout = binding.thumbsContainer
        container.removeAllViews()

        uiImages.forEachIndexed { index: Int, img: UiImage ->
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
        val size: Int = dp(116)
        val marginStart: Int = dp(8)
        val corner: Int = dp(12)

        val frame: FrameLayout = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                setMargins(marginStart, 0, 0, 0)
            }
            background = getDrawable(R.drawable.rectangle_11)
        }

        val iv: ImageView = ImageView(this).apply {
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

        val btnDel: ImageView = ImageView(this).apply {
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

    private fun updateImageCount(): Unit {
        binding.tvImageCount.text = "${uiImages.size}/$maxImages"
    }

    private fun dp(value: Int): Int =
        (resources.displayMetrics.density * value).toInt()

    // --------------- URL/URI → Multipart ---------------

    private fun urlToPart(url: String, partName: String, fallbackName: String): MultipartBody.Part? {
        return try {
            val req: Request = Request.Builder().url(url).build()
            val res = http.newCall(req).execute()
            if (!res.isSuccessful) return null
            val bytes: ByteArray = res.body?.bytes() ?: return null
            val mime: String = res.body?.contentType()?.toString() ?: guessMimeFromUrl(url)
            val fileName: String = url.substringAfterLast('/').ifBlank { "$fallbackName.jpg" }
            val rb: RequestBody = bytes.toRequestBody(mime.toMediaType())
            MultipartBody.Part.createFormData(partName, fileName, rb)
        } catch (_: Throwable) { null }
    }

    private fun uriToPart(uri: Uri, partName: String, fallbackName: String): MultipartBody.Part? {
        return try {
            val mime: String = contentResolver.getType(uri) ?: "image/jpeg"
            val fileName: String = queryDisplayName(uri) ?: "$fallbackName.jpg"
            val bytes: ByteArray = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val rb: RequestBody = bytes.toRequestBody(mime.toMediaType())
            MultipartBody.Part.createFormData(partName, fileName, rb)
        } catch (_: Throwable) { null }
    }

    private fun queryDisplayName(uri: Uri): String? = try {
        contentResolver.query(uri, null, null, null, null)?.use { c ->
            val idx: Int = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
        }
    } catch (_: Throwable) { null }

    private fun guessMimeFromUrl(url: String): String = when {
        url.endsWith(".png", true)  -> "image/png"
        url.endsWith(".webp", true) -> "image/webp"
        else                        -> "image/jpeg"
    }

    // --------------- 입력 헬퍼 ---------------

    private fun isDoneOrEnter(actionId: Int, event: KeyEvent?): Boolean =
        actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_GO ||
                actionId == EditorInfo.IME_ACTION_SEND ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP)

    private fun View.hideKeyboard(): Unit {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun EditText.clearOnEnter(confirm: (() -> Unit)? = null): Unit {
        imeOptions = EditorInfo.IME_ACTION_DONE
        setSingleLine(true)
        setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            val handled: Boolean = if (isDoneOrEnter(actionId, event)) {
                confirm?.invoke()
                text?.clear()
                v.hideKeyboard()
                true
            } else false
            handled
        })
        setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            val handled: Boolean = if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                confirm?.invoke()
                text?.clear()
                v.hideKeyboard()
                true
            } else false
            handled
        })
    }
}