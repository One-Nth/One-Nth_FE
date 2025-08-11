package com.example.onenthapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
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
import com.example.onenthapp.data.PlusRepository
import com.example.onenthapp.data.ShareRequest
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
        initTagInput()
        setupConfirmToggle()
        setupWayToggle()
        //setupValidation()
        binding.btnProductSubmit.setOnClickListener {
            setupSubmitButton()
        }
        binding.includeToolbar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
    private fun initTagInput() {
        binding.etProductTag.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.etProductTag.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val raw = binding.etProductTag.text.toString().trim()
                if (raw.isNotEmpty() && tags.size < 5) {
                    val normalized = if (raw.startsWith("#")) raw else "#$raw"
                    tags += normalized
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
            val chip = Chip(requireContext(), null, com.google.android.material.R.attr.chipStyle).apply {
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
            binding.tvLocationError.visibility = View.GONE
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
    /** 폼 유효성 검사 후 Retrofit 호출 */
    private fun setupSubmitButton() {
        // 1) 폼 값 읽기
        val title = binding.etProductName.text.toString()
        if (title.isEmpty()) {
            binding.tvNameError.text = "상품명을 입력해주세요"
            binding.tvNameError.visibility = View.VISIBLE
            binding.etProductName.requestFocus()
            return
        }
        val quantity = binding.etProductNum.text.toString().toIntOrNull()
        if (quantity == null || quantity < 1) {
            binding.tvNumError.text = "수량을 올바르게 입력하세요"
            binding.tvNumError.visibility = View.VISIBLE
            binding.etProductNum.requestFocus()
            return
        }
        val priceStr = binding.etProductCost.text.toString().trim()
        val price = priceStr.toIntOrNull()
        if (priceStr.isEmpty()) {
            binding.tvNumError.text = "가격을 입력해주세요"
            binding.tvNumError.visibility = View.VISIBLE
            binding.etProductCost.requestFocus()
            return
        }
        val categoryId = binding.cgCategoryShare.checkedChipId
        var category = ""
        when (categoryId) {
            R.id.chip_electronics -> category = "ELECTRONICS"
            R.id.chip_food -> category = "FOOD"
            R.id.chip_lifestyle -> category = "HOUSEHOLD"
            R.id.chip_clothing -> category = "CLOTHING"
            R.id.chip_misc -> category = "MISC"
//            else -> {
//                binding.tvCategoryError.text = "카테고리를 선택해주세요"
//                binding.tvCategoryError.visibility = View.VISIBLE                binding.cgCategoryShare.requestFocus() // 또는 적절한 뷰로 포커스
//                return // 제출 중단
//            }
        }
        val expiry = binding.etProductDue.text.toString().trim()
        if (binding.chipFood.isChecked) {
            if (expiry.isEmpty()) {
                binding.tvExpiryError.text = "유효기간을 입력해주세요."
                binding.tvExpiryError.visibility = View.VISIBLE
                binding.etProductDue.requestFocus()
                return
            }
        }
        val isAvailable = binding.btnConfirmYes.isChecked
        val isOffline = binding.btnWay1.isChecked
        val location = binding.etProductPlace.text.toString().trim()
        if (isOffline && location.isEmpty()) {
            binding.tvLocationError.text = "거래 장소를 입력해주세요."
            binding.tvLocationError.visibility = View.VISIBLE
            binding.etProductPlace.requestFocus()
            return
        }

        // 2) 요청 객체 생성
        val req = ShareRequest(
            title = title,
            quantity = quantity,
            price = price,
            itemCategory = category,               // 실제 선택값으로 교체
            expirationDate = expiry,
            isAvailable = isAvailable,
            purchaseMethod = if (binding.btnWay1.isChecked) "OFFLINE" else "ONLINE",
            sharingLocation = location,
            tags = tags             // 실제 태그 파싱 로직으로 교체
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
        // 2) Bundle 에 담아서 navigate
        val bundle = bundleOf(
            "productName" to title,
            "productPrice" to priceStr,
            //"productImageUri" to imageUri
        )
        // 3) 네트워크 호출
        lifecycleScope.launch {
            try {
                val resp = repo.createSharingItem(dataPart, parts)
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    // 4) 완료 화면으로 이동
                    findNavController().navigate(R.id.plusCompleteFragment, bundle)
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}