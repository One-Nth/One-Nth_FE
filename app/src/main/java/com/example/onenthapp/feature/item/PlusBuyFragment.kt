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

class PlusBuyFragment : Fragment() {
    private var _binding: FragmentPlusBuyBinding? = null
    private val binding get() = _binding!!
    private val repo = PlusRepository()
    private val tags = mutableListOf<String>()

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

//            // 만약 이미지 피커를 구현하셨다면, URI 를 String 으로 꺼내세요.
//            val imageUri = binding.ivPreviewImage.drawable.let {
//                // 예시: 실제 URI 를 String 으로 저장해두셨다면 여기에 꺼내서 넣어주세요.
//                ""
//            }
        //val imageUri = "imageUri"

        binding.includeToolbar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
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

            val realUris: List<Uri> = emptyList()
            val parts = if (realUris.isNotEmpty()) {
                realUris.mapIndexed { i, uri ->
                    val tmp = File(requireContext().cacheDir, "img_$i.jpg")
                    val rb = tmp.readBytes().toRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("imageFiles", tmp.name, rb)
                }
            } else {
                // drawable/mock_image.jpg 를 res/drawable 에 추가해 두세요
                val bmp = BitmapFactory.decodeResource(resources, R.drawable.image_tissue_2)
                val bos = ByteArrayOutputStream().apply {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 80, this)
                }
                val dummy = File(requireContext().cacheDir, "image_tissue_2.jpg")
                    .apply { writeBytes(bos.toByteArray()) }
                val rb = dummy.readBytes().toRequestBody("image/jpeg".toMediaType())
                listOf(MultipartBody.Part.createFormData("imageFiles", dummy.name, rb))
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
                            Toast.makeText(
                                requireContext(),
                                resp.body()?.message ?: "등록 실패",
                                Toast.LENGTH_SHORT
                            ).show()
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