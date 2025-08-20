package com.example.onenthapp.feature.board

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
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
import com.example.onenthapp.R
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.data.post.PostPayload
import com.example.onenthapp.databinding.ActivityLifetipsWriteBinding
import com.example.onenthapp.util.TokenManager
import com.example.onenthapp.data.map.MyRegionRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class CreateLifePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLifetipsWriteBinding
    private val myRegionRepo = MyRegionRepository()

    // 이미지(최대 5장)
    private val pickedUris: MutableList<Uri> = mutableListOf()
    private val maxImages: Int = 5

    // 태그
    private val tagList: MutableList<String> = mutableListOf()
    private val maxTags: Int = 5

    // 링크/장소 값(칩 1개 고정)
    private var linkValue: String? = null
    private var locationValue: String? = null
    
    // 할인/맛집 게시판용 주소와 장소명 분리
    private var addressValue: String? = null
    private var placeNameValue: String? = null

    // 타입
    private enum class PostType { LIFE_TIP, DISCOUNT, RESTAURANT }
    private fun parsePostType(raw: String?): PostType {
        return when (raw?.uppercase()) {
            "DISCOUNT" -> PostType.DISCOUNT
            "RESTAURANT" -> PostType.RESTAURANT
            else -> PostType.LIFE_TIP
        }
    }

    // 여러 장 선택
    private val pickImagesLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            if (!uris.isNullOrEmpty()) {
                val remain: Int = maxImages - pickedUris.size
                if (remain <= 0) {
                    Toast.makeText(this, "최대 ${maxImages}장까지 가능합니다.", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                val toAdd: List<Uri> = uris.filterNot { it in pickedUris }.take(remain)
                if (toAdd.isEmpty()) {
                    Toast.makeText(this, "추가할 수 있는 이미지가 없습니다.", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                pickedUris.addAll(toAdd)
            }
            updateImageCount()
            renderThumbnails()
        }

    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)
        binding = ActivityLifetipsWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val postType: PostType = parsePostType(intent.getStringExtra("postType"))
        applyUiFor(postType)

        // ✅ 태그 입력은 단일 라인 + actionDone 강제
        binding.etTags.apply {
            isSingleLine = true
            maxLines = 1
            imeOptions = EditorInfo.IME_ACTION_DONE
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            setHorizontallyScrolling(false)
        }

        // 상단바
        binding.ivBack.setOnClickListener { finish() }
        binding.ivNotification.setOnClickListener { submit(postType) }

        // 카메라 타일
        binding.cameraTile.setOnClickListener { pickImagesLauncher.launch("image/*") }

        // 태그 입력
        setupTagInput()

        // 링크/장소 입력 (엔터/포커스 아웃으로 확정 → 칩 생성)
        setupLinkInput(postType)
        setupLocationInput(postType)
        setupPlaceNameInput(postType)

        // 초기 렌더
        updateImageCount()
        renderThumbnails()
    }

    // ------- 타입별 UI -------
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
        }
        renderMetaUi(type)
    }

    // ------- 태그 -------
    private fun setupTagInput(): Unit = with(binding) {
        // 엔터/Done → 확정 후 비우기 (기존)
        etTags.onConfirmClear { value ->
            addTagIfPossible(value)
        }

        // 스페이스/콤마/엔터(개행) 자동 확정
        etTags.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = s?.toString().orEmpty()

                // ✅ 엔터가 개행으로 들어오는 케이스 처리
                if (str.contains('\n')) {
                    val v = str.replace("\n", "")
                    if (v.isNotBlank()) addTagIfPossible(v)
                    etTags.text?.clear()
                    return
                }

                // 스페이스/콤마로 자동 확정
                if (str.endsWith(" ") || str.endsWith(",")) {
                    addTagIfPossible(str.removeSuffix(" ").removeSuffix(","))
                    etTags.text?.clear()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun addTagIfPossible(raw: String) {
        val clean = raw.removePrefix("#").trim()
        if (clean.isBlank()) return
        if (tagList.size >= maxTags) {
            Toast.makeText(this, "태그는 최대 ${maxTags}개까지 가능합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (tagList.any { it.equals(clean, ignoreCase = true) }) return
        tagList.add(clean)
        addTagChip(clean)
    }


    private fun addTagChip(tag: String): Unit {
        val chip: Chip = Chip(this).apply {
            text = "# $tag"
            isCloseIconVisible = true
            chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#E7F6ED"))
            setTextColor(ContextCompat.getColor(this@CreateLifePostActivity, R.color.main_green))
            closeIconTint = ColorStateList.valueOf(ContextCompat.getColor(this@CreateLifePostActivity,
                R.color.main_green
            ))
            setOnCloseIconClickListener {
                tagList.remove(tag)
                binding.chipGroupTags.removeView(this)
            }
        }
        binding.chipGroupTags.addView(chip)
    }

    // ------- 링크/장소 칩 -------
    private fun setupLinkInput(type: PostType): Unit = with(binding) {
        etLink.setOnEditorActionListener { v, actionId, event ->
            val done: Boolean = isDoneOrEnter(actionId, event)
            if (done) { confirmLinkFromInput(type); v.hideKeyboard() }
            done
        }
        etLink.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) confirmLinkFromInput(type) }
    }

    private fun setupLocationInput(type: PostType): Unit = with(binding) {
        etLocation.setOnEditorActionListener { v, actionId, event ->
            val done: Boolean = isDoneOrEnter(actionId, event)
            if (done) { confirmAddressFromInput(type); v.hideKeyboard() }
            done
        }
        etLocation.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) confirmAddressFromInput(type) }
    }

    private fun setupPlaceNameInput(type: PostType): Unit = with(binding) {
        etPlaceName.setOnEditorActionListener { v, actionId, event ->
            val done: Boolean = isDoneOrEnter(actionId, event)
            if (done) { confirmPlaceNameFromInput(type); v.hideKeyboard() }
            done
        }
        etPlaceName.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) confirmPlaceNameFromInput(type) }
    }

    private fun confirmLinkFromInput(type: PostType): Unit {
        if (type != PostType.LIFE_TIP) return
        if (linkValue != null) { binding.etLink.text?.clear(); return }
        val v: String = binding.etLink.text?.toString()?.trim().orEmpty()
        if (v.isBlank()) return
        linkValue = v
        binding.etLink.text?.clear()
        renderMetaUi(type)
    }

    private fun confirmAddressFromInput(type: PostType): Unit {
        if (type != PostType.DISCOUNT && type != PostType.RESTAURANT) return
        if (addressValue != null) { binding.etLocation.text?.clear(); return }
        val v: String = binding.etLocation.text?.toString()?.trim().orEmpty()
        if (v.isBlank()) return
        addressValue = v
        binding.etLocation.text?.clear()
        renderMetaUi(type)
    }

    private fun confirmPlaceNameFromInput(type: PostType): Unit {
        if (type != PostType.DISCOUNT && type != PostType.RESTAURANT) return
        if (placeNameValue != null) { binding.etPlaceName.text?.clear(); return }
        val v: String = binding.etPlaceName.text?.toString()?.trim().orEmpty()
        if (v.isBlank()) return
        placeNameValue = v
        binding.etPlaceName.text?.clear()
        renderMetaUi(type)
    }

    private fun renderMetaUi(type: PostType): Unit = with(binding) {
        chipGroupLink.removeAllViews()
        chipGroupLocation.removeAllViews()

        when (type) {
            PostType.LIFE_TIP -> {
                val v: String = linkValue?.trim().orEmpty()
                if (v.isNotEmpty()) {
                    val onClose: () -> Unit = {
                        linkValue = null
                        renderMetaUi(type)
                        etLink.requestFocus()
                    }
                    chipGroupLink.addView(makeSingleChip(v, onClose))
                }
                etLink.visibility = if (linkValue == null) View.VISIBLE else View.GONE
                etLocation.visibility = View.GONE
            }
            PostType.DISCOUNT, PostType.RESTAURANT -> {
                // 장소명 칩
                val placeName: String = placeNameValue?.trim().orEmpty()
                if (placeName.isNotEmpty()) {
                    val onClosePlaceName: () -> Unit = {
                        placeNameValue = null
                        renderMetaUi(type)
                        etPlaceName.requestFocus()
                    }
                    chipGroupLocation.addView(makeSingleChip("📍 $placeName", onClosePlaceName))
                }
                
                // 주소 칩
                val address: String = addressValue?.trim().orEmpty()
                if (address.isNotEmpty()) {
                    val onCloseAddress: () -> Unit = {
                        addressValue = null
                        renderMetaUi(type)
                        etLocation.requestFocus()
                    }
                    chipGroupLocation.addView(makeSingleChip("🏠 $address", onCloseAddress))
                }
                
                etPlaceName.visibility = if (placeNameValue == null) View.VISIBLE else View.GONE
                etLocation.visibility = if (addressValue == null) View.VISIBLE else View.GONE
                etLink.visibility = View.GONE
            }
        }
    }

    private fun makeSingleChip(textValue: String, onClose: () -> Unit): Chip =
        Chip(this).apply {
            text = textValue
            isCloseIconVisible = true
            chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#E7F6ED"))
            setTextColor(ContextCompat.getColor(this@CreateLifePostActivity, R.color.main_green))
            closeIconTint = ColorStateList.valueOf(ContextCompat.getColor(this@CreateLifePostActivity,
                R.color.main_green
            ))
            setOnCloseIconClickListener {
                onClose()
                (parent as? ChipGroup)?.removeView(this) // 안전망
            }
        }

    // ------- 이미지 썸네일 -------
    private fun updateImageCount(): Unit { binding.tvImageCount.text = "${pickedUris.size}/$maxImages" }

    private fun renderThumbnails(): Unit {
        val container: LinearLayout = binding.thumbsContainer
        container.removeAllViews()
        pickedUris.forEachIndexed { index: Int, uri: Uri ->
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

    private fun createThumbFrame(
        uri: Uri,
        index: Int,
        onRemove: () -> Unit
    ): View {
        val size: Int = dp(116)
        val marginStart: Int = dp(8)
        val corner: Int = dp(12)

        val frame: FrameLayout = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(size, size).apply { setMargins(marginStart, 0, 0, 0) }
            background = getDrawable(R.drawable.rectangle_11)
        }

        val iv: ImageView = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        Glide.with(this).load(uri).transform(CenterCrop(), RoundedCorners(corner)).into(iv)
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

    // ------- 글 등록 -------
    private fun submit(type: PostType): Unit {
        val title: String = binding.etTitle.text?.toString()?.trim().orEmpty()
        val content: String = binding.etContent.text?.toString()?.trim().orEmpty()

        // 최신 입력값 반영(칩이 없고 입력창에만 있을 수 있으므로)
        if (type == PostType.LIFE_TIP && linkValue.isNullOrBlank()) {
            val v: String = binding.etLink.text?.toString()?.trim().orEmpty()
            if (v.isNotBlank()) linkValue = v
        }
        if ((type == PostType.DISCOUNT || type == PostType.RESTAURANT) && locationValue.isNullOrBlank()) {
            val v: String = binding.etLocation.text?.toString()?.trim().orEmpty()
            if (v.isNotBlank()) locationValue = v
        }

        if (title.isBlank() || content.isBlank()) {
            Toast.makeText(this, "제목과 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show(); return
        }

        val tags: List<String> = tagList.toList()

        val token: String? = TokenManager.getAccessToken()
        if (token.isNullOrEmpty()) { Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show(); return }

        // 할인/맛집 게시판에서는 메인 지역 ID 가져오기
        if (type == PostType.DISCOUNT || type == PostType.RESTAURANT) {
            setLoading(true)
            lifecycleScope.launch {
                try {
                    val regions = myRegionRepo.getMyRegions()
                    val mainRegion = regions.find { it.main }
                    
                    if (mainRegion == null) {
                        setLoading(false)
                        Toast.makeText(this@CreateLifePostActivity, "메인 지역이 설정되지 않았습니다.\n내 지역을 먼저 설정해주세요.", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    
                    // 메인 지역 ID를 포함한 payload 생성
                    val payload = PostPayload(
                        title = title,
                        content = content,
                        address = addressValue?.ifBlank { null },
                        placeName = placeNameValue?.ifBlank { null },
                        regionId = mainRegion.regionId,
                        tags = tags
                    )
                    
                    proceedWithPost(type, payload, token)
                    
                } catch (e: Exception) {
                    setLoading(false)
                    Toast.makeText(this@CreateLifePostActivity, "지역 정보를 확인할 수 없습니다: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            return
        }
        
        // 생활꿀팁은 바로 등록 (regionId 없음)
        val payload = PostPayload(
            title = title,
            content = content,
            link = linkValue?.ifBlank { null },
            tags = tags
        )
        
        proceedWithPost(type, payload, token)
    }

    private fun proceedWithPost(type: PostType, payload: PostPayload, token: String) {
        val postPart: RequestBody = buildPostJsonPart(payload)
        val imageParts: List<MultipartBody.Part> = applicationContext.buildImageParts(pickedUris.take(maxImages))
        val imagesArg: List<MultipartBody.Part>? = if (imageParts.isEmpty()) null else imageParts

        setLoading(true)
        lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.postApi.createPost(
                    bearer = "Bearer $token",
                    postType = type.name,
                    postJson = postPart,
                    images = imagesArg
                )
                setLoading(false)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    if (body?.isSuccess == true) {
                        Toast.makeText(this@CreateLifePostActivity, "등록 완료", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@CreateLifePostActivity, "등록 실패: ${body?.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val err: String? = resp.errorBody()?.string()
                    Toast.makeText(this@CreateLifePostActivity, "HTTP ${resp.code()} 실패: $err", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(this@CreateLifePostActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLoading(loading: Boolean): Unit {
        binding.ivNotification.isEnabled = !loading
        // binding.progress.isVisible = loading
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun buildPostJsonPart(payload: PostPayload): RequestBody {
        val json: String = Gson().toJson(payload)
        return json.toRequestBody("application/json; charset=utf-8".toMediaType())
    }

    // 이미지 멀티파트 변환 (키 이름은 "images")
    fun Context.buildImageParts(uris: List<Uri>): List<MultipartBody.Part> {
        val parts: MutableList<MultipartBody.Part> = mutableListOf()
        for ((i, uri) in uris.withIndex()) {
            val mime: String = contentResolver.getType(uri) ?: "image/*"
            val bytes: ByteArray = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: continue
            val rb: RequestBody = bytes.toRequestBody(mime.toMediaTypeOrNull())
            val fileName: String = "image_${i}.jpg"
            parts += MultipartBody.Part.createFormData("images", fileName, rb)
        }
        return parts
    }

    private fun EditText.onConfirmClear(
        confirm: (String) -> Unit
    ) {
        fun runConfirm(view: View): Boolean {
            val value = text?.toString()?.trim().orEmpty()
            if (value.isNotEmpty()) confirm(value)
            text?.clear()                 // ✅ 엔터 후 항상 비우기
            view.hideKeyboard()
            return true
        }

        setOnEditorActionListener { v, actionId, event ->
            if (isDoneOrEnter(actionId, event)) runConfirm(v) else false
        }
        setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) runConfirm(v) else false
        }
    }


    // ------- 공용 헬퍼 -------
    private fun isDoneOrEnter(actionId: Int, event: KeyEvent?): Boolean =
        actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_GO ||
                actionId == EditorInfo.IME_ACTION_SEND ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP)

    private fun View.hideKeyboard(): Unit {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}
