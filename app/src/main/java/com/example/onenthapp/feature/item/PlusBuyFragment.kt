package com.example.onenthapp.feature.item

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.onenthapp.databinding.FragmentPlusBuyBinding
import androidx.navigation.fragment.findNavController
import com.example.onenthapp.data.item.PlusRepository
import com.example.onenthapp.data.item.BuyRequest
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory
import android.graphics.Bitmap // bmp.compress를 위해 필요
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import com.example.onenthapp.R
import com.google.android.material.chip.Chip
import okhttp3.MultipartBody // MultipartBody.Part를 위해 필요
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.view.Gravity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class PlusBuyFragment : Fragment() {
    private var _binding: FragmentPlusBuyBinding? = null
    private val binding get() = _binding!!
    private val repo = PlusRepository()
    private val tags = mutableListOf<String>()
    
    // 이미지 관련 변수들
    private val imageUris = mutableListOf<Uri>()
    private val maxImages = 3

    // 이미지 선택을 위한 ActivityResultLauncher
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            if (!uris.isNullOrEmpty()) {
                val remain: Int = maxImages - imageUris.size
                if (remain <= 0) {
                    Toast.makeText(requireContext(), "사진은 최대 ${maxImages}장까지 첨부할 수 있어요.", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                val toAdd: List<Uri> = uris.filterNot { it in imageUris }.take(remain)
                if (toAdd.isEmpty()) {
                    Toast.makeText(requireContext(), "추가할 수 있는 이미지가 없습니다.", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                imageUris.addAll(toAdd)
            }
            updateImageCount()
            renderThumbnails()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlusBuyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 엔터로 다음 포커스 이동
        binding.etProductName.imeOptions = EditorInfo.IME_ACTION_NEXT
        binding.etProductName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                // 온라인 버튼으로 포커스 이동
                binding.btnWay1.requestFocus()
                true
            } else false
        }
        initTagInput()
        setupToggleButtons()
        setupValidation()
        setupSubmitButton()
        setupImagePicker()

        binding.includeToolbar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        
        // 초기 이미지 상태 설정
        updateImageCount()
        renderThumbnails()
    }

    private fun initTagInput() {
        binding.etProductTag.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val input = binding.etProductTag.text.toString().trim()
                if (input.isNotEmpty() && tags.size < 5) {
                    val normalized = if (input.startsWith("#")) input else "#$input"
                    tags.add(normalized)
                    binding.etProductTag.text?.clear()
                    renderTags()
                }
                true
            } else {
                false
            }
        }
    }

    private fun renderTags() {
        binding.cgTags.removeAllViews()
        tags.forEach { tagText ->
            val chip = Chip(requireContext(), null, R.style.Widget_App_TagChip).apply {
                text = tagText
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    tags.remove(tagText)
                    renderTags()
                }
            }
            binding.cgTags.addView(chip)
        }
    }

    // 이미지 관련 메서드들
    private fun setupImagePicker() {
        binding.phImageUploadContainer.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun updateImageCount() {
        binding.tvImageCount.text = "${imageUris.size}/$maxImages"
    }

    private fun renderThumbnails() {
        val container: LinearLayout = binding.thumbsContainer
        container.removeAllViews()
        imageUris.forEachIndexed { index: Int, uri: Uri ->
            container.addView(
                createThumbFrame(
                    uri = uri,
                    index = index,
                    onRemove = {
                        imageUris.removeAt(index)
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

        val frame: FrameLayout = FrameLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(size, size).apply { setMargins(marginStart, 0, 0, 0) }
            background = resources.getDrawable(R.drawable.rectangle_11, null)
        }

        val iv: ImageView = ImageView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        Glide.with(this).load(uri).transform(CenterCrop(), RoundedCorners(corner)).into(iv)
        frame.addView(iv)

        val btnDel: ImageView = ImageView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(dp(22), dp(22)).apply {
                gravity = Gravity.END or Gravity.TOP
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

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun setupSubmitButton() {
        binding.btnProductSubmit.setOnClickListener {
            // 1) 폼 값 읽기
            val name = binding.etProductName.text.toString()
            if (name.isEmpty()) {
                binding.tvNameError.text = "상품명을 입력해주세요."
                binding.tvNameError.visibility = View.VISIBLE
                binding.etProductName.requestFocus()
                return@setOnClickListener
            }
            
            // 이미지 필수 체크
            if (imageUris.isEmpty()) {
                Toast.makeText(requireContext(), "상품 사진을 최소 1장 이상 등록해주세요.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            
            // 태그 필수 체크
            if (tags.isEmpty()) {
                Toast.makeText(requireContext(), "상품 태그를 최소 1개 이상 입력해주세요.", Toast.LENGTH_LONG).show()
                binding.etProductTag.requestFocus()
                return@setOnClickListener
            }
            val categoryId = binding.cgCategoryShare.checkedChipId
            var category = ""
            when (categoryId) {
                R.id.chip_electronics -> category = "ELECTRONICS"
                R.id.chip_food -> category = "FOOD"
                R.id.chip_lifestyle -> category = "HOUSEHOLD"
                R.id.chip_clothing -> category = "CLOTHING"
                R.id.chip_misc -> category = "MISC"
                else -> {
                    // Toast.makeText(requireContext(), "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
                    binding.cgCategoryShare.requestFocus() // 또는 적절한 뷰로 포커스
                    return@setOnClickListener // 제출 중단
                }
            }
            val priceStr = binding.etProductCost.text.toString().trim()
            val price = priceStr.toIntOrNull()
            if (priceStr.isEmpty()) {
                binding.tvPriceError.text = "가격을 입력해주세요."
                binding.tvPriceError.visibility = View.VISIBLE
                binding.etProductCost.requestFocus()
                return@setOnClickListener
            }
            val url = binding.etProductUrl.text.toString().trim()
            if (url.isEmpty()) {
                binding.tvUrlError.text = "링크를 입력해주세요."
                binding.tvUrlError.visibility = View.VISIBLE
                binding.etProductUrl.requestFocus()
                return@setOnClickListener
            }
            val isOffline = binding.btnWay2.isChecked
            val location = binding.etProductLocation.text.toString().trim()
            if (isOffline && location.isEmpty()) {
                binding.tvLocationError.text = "구매 장소를 입력해주세요."
                binding.tvLocationError.visibility = View.VISIBLE
                binding.etProductLocation.requestFocus()
                return@setOnClickListener
            }

            val expiry = binding.etProductDue.text.toString().trim()
            if (binding.chipFood.isChecked) {
                if (expiry.isEmpty()) {
                    binding.tvExpiryError.text = "유효기간을 입력해주세요."
                    binding.tvExpiryError.visibility = View.VISIBLE
                    binding.etProductDue.requestFocus()
                    return@setOnClickListener
                }
//                if(!Regex("""\d{4}-\d{2}-\d{2}""").matches(expiry)) {
//                    binding.tvExpiryError.text = "날짜 형식은 YYYY-MM-DD 이어야 합니다"
//                    binding.tvExpiryError.visibility = View.VISIBLE
//                    binding.etProductDue.requestFocus()
//                    return@setOnClickListener
//                }
            }
            // 2) 요청 객체 생성
            val req = BuyRequest(
                name = name,
                purchaseMethod = if (binding.btnWay1.isChecked) "ONLINE" else "OFFLINE",
                itemCategory = category,               // 실제 선택값으로 교체
                purchaseUrl = url,
                purchaseLocation = location,                        // 해당 탭엔 Location 없음
                price = price,
                tags = tags,             // 실제 태그 파싱 로직으로 교체
                expirationDate = expiry
            )
            // JSON → RequestBody
            val json = Gson().toJson(req)
            val dataPart = json.toRequestBody("application/json".toMediaType())

            val realUris: List<Uri> = imageUris.toList()
            val parts = if (realUris.isNotEmpty()) {
                realUris.mapIndexed { i, uri ->
                    val tmp = File(requireContext().cacheDir, "img_$i.jpg")
                    // URI를 임시 파일로 복사
                    requireContext().contentResolver.openInputStream(uri)?.use { input ->
                        tmp.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    val rb = tmp.readBytes().toRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("imageFiles", tmp.name, rb)
                }
            } else {
                // 이미지가 없는 경우 빈 리스트 반환
                emptyList()
            }
//            val newId = resp.body()!!.result.id
//            // 2) Bundle 에 담아서 navigate
//            val bundle = bundleOf(
//                "productName" to name,
//                "productPrice" to priceStr,
//                //"productImageUri" to realUris,
//                "productId" to newId,
//                "isBuy" to true
//            )
            // 3) 네트워크 호출
            lifecycleScope.launch {
                try {
                    val resp = repo.createGroupPurchase(dataPart, parts)
                    if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                        val responseBody = resp.body()
                        if (responseBody != null) {
                            val newId =
                                responseBody.result.purchaseItemId // result와 id가 null이 아님을 보장

                            // 3) Bundle 생성 (이제 newId 사용 가능)
                            val bundle = bundleOf(
                                "productName" to name,       // launch 블록 외부에서 정의된 값 사용
                                "productPrice" to priceStr,  // launch 블록 외부에서 정의된 값 사용
                                // "firstImageUriString" to realUris.firstOrNull()?.toString(), // 필요시
                                "productId" to newId,        // 여기서 얻은 newId 사용
                                "isBuy" to true,
                                "firstImageUrl" to realUris.firstOrNull()?.toString() // 첫 번째 이미지 정보
                            )
                            // 4) 완료 화면으로 이동
                            findNavController().navigate(R.id.plusCompleteFragment, bundle)
                        } else {
                            // 응답 본문 또는 result 객체가 null인 경우의 오류 처리
                            Log.e("PlusBuy", "서버 응답 성공했으나, body 또는 result가 null입니다.")
                            Toast.makeText(requireContext(), "등록 결과를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                            val code = resp.code()
                            val errBody = resp.errorBody()?.string().orEmpty()
                            Log.e("PlusBuy", "서버 오류: HTTP $code / $errBody")
                            
                            // 더 상세한 에러 메시지 제공
                            val errorMessage = when (code) {
                                400 -> "잘못된 요청입니다. 입력 정보를 확인해주세요."
                                401 -> "로그인이 필요합니다. 다시 로그인해주세요."
                                403 -> "권한이 없습니다. 관리자에게 문의해주세요."
                                404 -> "요청한 리소스를 찾을 수 없습니다."
                                500 -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                                else -> resp.body()?.message ?: "상품 등록에 실패했습니다. (오류 코드: $code)"
                            }
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "네트워크 오류: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        private fun setupValidation() {
            val watcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    validatePrice()
                    //validateLocation()
                    validateExpiry()
                    //validateTags()
                    binding.btnProductSubmit.isEnabled = isFormValid()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

            with(binding) {
                etProductCost.addTextChangedListener(watcher)  // 가격
                //etProductLocation .addTextChangedListener(watcher)  // 장소
                etProductDue.addTextChangedListener(watcher)  // 만료일
                etProductTag.addTextChangedListener(watcher)  // 태그
            }

//        // 카테고리(ChipGroup) 선택 시에도 만료일 검증
//        binding.cgCategoryShare.setOnCheckedChangeListener { _, checkedId ->
//            validateExpiry()
//            binding.btnProductSubmit.isEnabled = isFormValid()
//        }
        }

        private fun setupToggleButtons() {
            // online 버튼 클릭시
            binding.btnWay1.setOnClickListener {
                // 토글 버튼 상태 변경
                binding.btnWay1.isChecked = true
                binding.btnWay2.isChecked = false
                // location 입력 불가
                binding.etProductLocation.isEnabled = false
                binding.etProductLocation.setText("")
                binding.tvLocationError.text = "온라인 구매 시 장소를 입력할 수 없습니다."
                binding.tvLocationError.visibility = View.VISIBLE
            }
            // offline 클릭시 location 필수
            binding.btnWay2.setOnClickListener {
                binding.btnWay2.isChecked = true
                binding.btnWay1.isChecked = false
                binding.etProductLocation.isEnabled = true
                binding.tvLocationError.visibility = View.GONE
            }
        }

        // 입력 후 로직
        private fun validatePrice() {
            val str = binding.etProductCost.text.toString().trim()
            if (str.isNotEmpty() && str.toIntOrNull() == null) {
                binding.tvPriceError.text = "가격을 숫자로 입력하세요"
                binding.tvPriceError.visibility = View.VISIBLE
            } else {
                binding.tvPriceError.visibility = View.GONE
            }
        }

//    // 오프라인 null 검사는 제출 시 점검 로직으로 수정
//    private fun validateLocation() {
//        if (binding.btnWay2.isChecked) {
//            // OFFLINE 일 때만 필수
//            if (binding.etProductLocation.text.toString().trim().isEmpty()) {
//                binding.tvLocationError.text       = "구매 장소를 입력해주세요"
//                binding.tvLocationError.visibility = View.VISIBLE
//            } else {
//                binding.tvLocationError.visibility = View.GONE
//            }
//        }
//    }

        private fun validateExpiry() {
            // FOOD 선택 시에만 체크
            val isFood = binding.chipFood.isChecked
            val raw = binding.etProductDue.text.toString().trim()
            if (isFood) {
                if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(raw)) {
                    binding.tvExpiryError.text = "날짜 형식은 YYYY-MM-DD 이어야 합니다"
                    binding.tvExpiryError.visibility = View.VISIBLE
                } else {
                    binding.tvExpiryError.visibility = View.GONE
                }
            } else {
                // FOOD 외엔 입력 불필요
                binding.tvExpiryError.text = "음식 외의 카테고리는 기한을 입력할 수 없습니다"
                // binding.etProductDue.setText("")
                binding.tvExpiryError.visibility = View.VISIBLE
            }
        }

//        private fun validateTags() {
//            val tag = binding.etProductTag.text.toString().trim()
//            if (tag.isNotEmpty() && !tag.startsWith("#")) {
//                binding.tvTagsError.text = "태그는 반드시 #으로 시작해야 합니다"
//                binding.tvTagsError.visibility = View.VISIBLE
//            } else {
//                binding.tvTagsError.visibility = View.GONE
//            }
//        }

        private fun isFormValid(): Boolean {
            val priceOk = binding.etProductCost.text.toString().toIntOrNull() != null
            val locOk = if (binding.btnWay2.isChecked)
                binding.etProductLocation.text.toString().trim().isNotEmpty()
            else true
            val tagsOk =
                binding.etProductTag.text.toString().let { it.isEmpty() || it.startsWith("#") }
            val expiryOk = if (binding.chipFood.isChecked)
                Regex("""\d{4}-\d{2}-\d{2}""")
                    .matches(binding.etProductDue.text.toString().trim())
            else true
            return priceOk && locOk && tagsOk && expiryOk
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }