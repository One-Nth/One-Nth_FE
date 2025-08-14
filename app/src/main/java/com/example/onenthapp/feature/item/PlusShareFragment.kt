package com.example.onenthapp.feature.item

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.onenthapp.databinding.FragmentPlusShareBinding
import androidx.navigation.fragment.findNavController
import com.example.onenthapp.R
import com.example.onenthapp.data.item.PlusRepository
import com.example.onenthapp.data.item.ShareRequest
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File

class PlusShareFragment : Fragment() {
    private var _binding: FragmentPlusShareBinding? = null
    private val binding get() = _binding!!
    private val repo = PlusRepository()
    private val tags = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlusShareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 엔터로 다음 포커스 이동
        binding.etProductName.imeOptions = EditorInfo.IME_ACTION_NEXT
        binding.etProductName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                // 수량 입력 필드로 포커스 이동
                binding.etProductNum.requestFocus()
                true
            } else false
        }
        
        initTagInput()
        setupConfirmToggle()
        setupWayToggle()
        setupValidation()
        setupSubmitButton()

        binding.includeToolbar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initTagInput() {
        binding.etProductTag.imeOptions = EditorInfo.IME_ACTION_DONE
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
            } else false
        }
    }

    /** ChipGroup 에 칩으로 렌더링 */
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

    private fun setupConfirmToggle() {
        binding.btnConfirmYes.setOnClickListener {
            binding.btnConfirmYes.isChecked = true
            binding.btnConfirmNoCancel.isChecked = false
        }
        binding.btnConfirmNoCancel.setOnClickListener {
            binding.btnConfirmNoCancel.isChecked = true
            binding.btnConfirmYes.isChecked = false
        }
    }

    private fun setupWayToggle() {
        binding.btnWay1.setOnClickListener {
            binding.btnWay1.isChecked = true
            binding.btnWay2.isChecked = false
            binding.etProductPlace.isEnabled = true
            //binding.tvLocationError.visibility = View.GONE
        }
        binding.btnWay2.setOnClickListener {
            binding.btnWay2.isChecked = true
            binding.btnWay1.isChecked = false
            binding.etProductPlace.setText("")
            binding.etProductPlace.isEnabled = false
            binding.tvLocationError.text = "택배 거래는 장소 입력이 불가능합니다"
            binding.tvLocationError.visibility = View.VISIBLE
        }
    }

    private fun setupValidation() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validatePrice()
                validateQuantity()
                validateExpiry()
                binding.btnProductSubmit.isEnabled = isFormValid()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        with(binding) {
            etProductCost.addTextChangedListener(watcher)  // 가격
            etProductNum.addTextChangedListener(watcher)   // 수량
            etProductDue.addTextChangedListener(watcher)   // 만료일
            etProductTag.addTextChangedListener(watcher)   // 태그
        }

    }

    private fun validatePrice() {
        val str = binding.etProductCost.text.toString().trim()
        if (str.isNotEmpty() && str.toIntOrNull() == null) {
            binding.tvNumError.text = "가격을 숫자로 입력하세요"
            binding.tvNumError.visibility = View.VISIBLE
        } else {
            binding.tvNumError.visibility = View.GONE
        }
    }

    private fun validateQuantity() {
        val str = binding.etProductNum.text.toString().trim()
        if (str.isNotEmpty() && str.toIntOrNull() == null) {
            binding.tvNumError.text = "수량을 숫자로 입력하세요"
            binding.tvNumError.visibility = View.VISIBLE
        } else {
            binding.tvNumError.visibility = View.GONE
        }
    }

    private fun validateExpiry() {
        // FOOD 선택 시에만 체크
        val isFood = binding.chipFood.isChecked
        val raw = binding.etProductDue.text.toString().trim()
        if (isFood) {
            if (raw.isNotEmpty() && !Regex("""\d{4}-\d{2}-\d{2}""").matches(raw)) {
                binding.tvExpiryError.text = "날짜 형식은 YYYY-MM-DD 이어야 합니다"
                binding.tvExpiryError.visibility = View.VISIBLE
            } else {
                binding.tvExpiryError.visibility = View.GONE
            }
        } else {
            // FOOD 외엔 입력 불필요
            binding.tvExpiryError.text = "음식 외의 카테고리는 기한을 입력할 수 없습니다"
            binding.tvExpiryError.visibility = View.VISIBLE
        }
    }

    private fun isFormValid(): Boolean {
        val priceOk = binding.etProductCost.text.toString().toIntOrNull() != null
        val quantityOk = binding.etProductNum.text.toString().toIntOrNull() != null
        val locOk = if (binding.btnWay1.isChecked)
            binding.etProductPlace.text.toString().trim().isNotEmpty()
        else true
        val tagsOk = binding.etProductTag.text.toString().let { it.isEmpty() || it.startsWith("#") }
        val expiryOk = if (binding.chipFood.isChecked)
            Regex("""\d{4}-\d{2}-\d{2}""")
                .matches(binding.etProductDue.text.toString().trim())
        else true
        return priceOk && quantityOk && locOk && tagsOk && expiryOk
    }

    /** 폼 유효성 검사 후 Retrofit 호출 */
    private fun setupSubmitButton() {
        binding.btnProductSubmit.setOnClickListener {
            // 1) 폼 값 읽기
            val title = binding.etProductName.text.toString()
            if (title.isEmpty()) {
                binding.tvNameError.text = "상품명을 입력해주세요"
                binding.tvNameError.visibility = View.VISIBLE
                binding.etProductName.requestFocus()
                return@setOnClickListener
            }
            
            val quantity = binding.etProductNum.text.toString().toIntOrNull()
            if (quantity == null || quantity < 1) {
                binding.tvNumError.text = "수량을 올바르게 입력하세요"
                binding.tvNumError.visibility = View.VISIBLE
                binding.etProductNum.requestFocus()
                return@setOnClickListener
            }
            
            val priceStr = binding.etProductCost.text.toString().trim()
            val price = priceStr.toIntOrNull()
            if (priceStr.isEmpty()) {
                binding.tvNumError.text = "가격을 입력해주세요"
                binding.tvNumError.visibility = View.VISIBLE
                binding.etProductCost.requestFocus()
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
                    binding.tvCategoryError.text = "카테고리를 선택해주세요"
                    binding.tvCategoryError.visibility = View.VISIBLE
                    binding.cgCategoryShare.requestFocus()
                    return@setOnClickListener
                }
            }
            
            val expiry = binding.etProductDue.text.toString().trim()
            if (binding.chipFood.isChecked && expiry.isEmpty()) {
                binding.tvExpiryError.text = "유효기간을 입력해주세요."
                binding.tvExpiryError.visibility = View.VISIBLE
                binding.etProductDue.requestFocus()
                return@setOnClickListener
            }
            
            val isAvailable = binding.btnConfirmYes.isChecked
            val isOffline = binding.btnWay1.isChecked
            val location = binding.etProductPlace.text.toString().trim()
            if (isOffline && location.isEmpty()) {
                binding.tvLocationError.text = "거래 장소를 입력해주세요."
                binding.tvLocationError.visibility = View.VISIBLE
                binding.etProductPlace.requestFocus()
                return@setOnClickListener
            }

            // 2) 요청 객체 생성
            val req = ShareRequest(
                title = title,
                quantity = quantity,
                price = price,
                itemCategory = category,
                expirationDate = expiry,
                isAvailable = isAvailable,
                purchaseMethod = if (binding.btnWay1.isChecked) "OFFLINE" else "ONLINE",
                sharingLocation = location,
                tags = tags
            )
            
            // JSON → RequestBody
            val json = Gson().toJson(req)
            val dataPart = json.toRequestBody("application/json".toMediaType())

            // 3) 이미지 파트 (실제 업로드 미구현 상태라 dummy 이미지 하나 강제)
            val realUris: List<Uri> = emptyList() // TODO: 실제 Uri 리스트
            val parts = if (realUris.isNotEmpty()) {
                realUris.mapIndexed { i, uri ->
                    val tmp = File(requireContext().cacheDir, "img_tissue_$i.jpg")
                    // TODO: uri → tmp 파일 복사
                    val rb = tmp.readBytes().toRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("imageFiles", tmp.name, rb)
                }
            } else {
                // drawable/mock_image.jpg 를 res/drawable 에 추가해 두세요
                val bmp = BitmapFactory.decodeResource(resources, R.drawable.image_tissue_1)
                val bos = ByteArrayOutputStream().apply {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 80, this)
                }
                val dummy = File(requireContext().cacheDir, "image_tissue_1.jpg")
                    .apply { writeBytes(bos.toByteArray()) }
                val rb = dummy.readBytes().toRequestBody("image/jpeg".toMediaType())
                listOf(MultipartBody.Part.createFormData("imageFiles", dummy.name, rb))
            }
            
            // 4) 네트워크 호출
            lifecycleScope.launch {
                try {
                    val resp = repo.createSharingItem(dataPart, parts)
                    if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                        val responseBody = resp.body()
                        if (responseBody != null) {
                            val newId = responseBody.result.sharingItemId
                            
                            // 5) Bundle 생성하여 완료 화면으로 이동
                            val bundle = bundleOf(
                                "productName" to title,
                                "productPrice" to priceStr,
                                "productId" to newId,
                                "isBuy" to false,
                                "firstImageUrl" to "image_tissue_1" // 첫 번째 이미지 정보
                            )
                            
                            findNavController().navigate(R.id.plusCompleteFragment, bundle)
                        } else {
                            Toast.makeText(requireContext(), "등록 결과를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val code = resp.code()
                        val errBody = resp.errorBody()?.string().orEmpty()
                        Log.e("PlusShare", "서버 오류: HTTP $code / $errBody")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}