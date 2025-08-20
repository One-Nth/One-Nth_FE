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
import com.example.onenthapp.data.map.MyRegionRepository
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.view.Gravity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class PlusShareFragment : Fragment() {
    private var _binding: FragmentPlusShareBinding? = null
    private val binding get() = _binding!!
    private val repo = PlusRepository()
    private val myRegionRepo = MyRegionRepository()
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
        setupImagePicker()

        binding.includeToolbar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        
        // 우리동네로 설정 버튼 클릭 이벤트
        binding.btnSetMyplace.setOnClickListener {
            setMyRegionAsLocation()
        }
        
        // 초기 이미지 상태 설정
        updateImageCount()
        renderThumbnails()
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

    /** 우리동네로 설정 버튼 클릭 시 메인지역 주소를 거래 장소에 자동 입력 */
    private fun setMyRegionAsLocation() {
        lifecycleScope.launch {
            try {
                val myRegions = myRegionRepo.getMyRegions()
                val mainRegion = myRegions.find { it.main }
                
                if (mainRegion != null) {
                    binding.etProductPlace.setText(mainRegion.regionName)
                    Toast.makeText(requireContext(), "우리동네 주소가 입력되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "설정된 메인지역이 없습니다. 마이페이지에서 지역을 설정해주세요.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("PlusShare", "메인지역 조회 실패", e)
                Toast.makeText(requireContext(), "지역 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
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
                binding.tvExpiryError.text = "날짜 형식은 YYYY-MM-DD 이어야 합니다."
                binding.tvExpiryError.visibility = View.VISIBLE
            } else {
                binding.tvExpiryError.visibility = View.GONE
            }
        } else {
            // FOOD 외엔 입력 불필요
            binding.tvExpiryError.text = "음식 외의 카테고리는 기한을 입력할 수 없습니다."
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

    /** 모든 에러메시지 초기화 */
    private fun clearAllErrors() {
        binding.tvNameError.visibility = View.GONE
        binding.tvNumError.visibility = View.GONE
        binding.tvCategoryError.visibility = View.GONE
        binding.tvExpiryError.visibility = View.GONE
        binding.tvLocationError.visibility = View.GONE
    }

    /** 폼 유효성 검사 후 Retrofit 호출 */
    private fun setupSubmitButton() {
        binding.btnProductSubmit.setOnClickListener {
            // 에러메시지 초기화
            clearAllErrors()
            
            // 1) 폼 값 읽기
            val title = binding.etProductName.text.toString().trim()
            if (title.isEmpty()) {
                binding.tvNameError.text = "상품명을 입력해주세요"
                binding.tvNameError.visibility = View.VISIBLE
                binding.etProductName.requestFocus()
                return@setOnClickListener
            }
            
            // 이미지 필수 체크
            if (imageUris.isEmpty()) {
                Toast.makeText(requireContext(), "상품 사진을 최소 1장 이상 등록해주세요", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            
            // 태그 필수 체크
            if (tags.isEmpty()) {
                Toast.makeText(requireContext(), "상품 태그를 최소 1개 이상 입력해주세요", Toast.LENGTH_LONG).show()
                binding.etProductTag.requestFocus()
                return@setOnClickListener
            }
            
            val quantityStr = binding.etProductNum.text.toString().trim()
            if (quantityStr.isEmpty()) {
                binding.tvNumError.text = "수량을 입력해주세요"
                binding.tvNumError.visibility = View.VISIBLE
                binding.etProductNum.requestFocus()
                return@setOnClickListener
            }
            val quantity = quantityStr.toIntOrNull()
            if (quantity == null) {
                binding.tvNumError.text = "수량을 숫자로 입력하세요"
                binding.tvNumError.visibility = View.VISIBLE
                binding.etProductNum.requestFocus()
                return@setOnClickListener
            }
            if (quantity < 1) {
                binding.tvNumError.text = "수량은 1개 이상이어야 합니다"
                binding.tvNumError.visibility = View.VISIBLE
                binding.etProductNum.requestFocus()
                return@setOnClickListener
            }
            
            val priceStr = binding.etProductCost.text.toString().trim()
            if (priceStr.isEmpty()) {
                binding.tvNumError.text = "가격을 입력해주세요"
                binding.tvNumError.visibility = View.VISIBLE
                binding.etProductCost.requestFocus()
                return@setOnClickListener
            }
            val price = priceStr.toIntOrNull()
            if (price == null) {
                binding.tvNumError.text = "가격을 숫자로 입력하세요"
                binding.tvNumError.visibility = View.VISIBLE
                binding.etProductCost.requestFocus()
                return@setOnClickListener
            }
            if (price < 1) {
                binding.tvNumError.text = "가격은 1원 이상이어야 합니다"
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
                binding.tvExpiryError.text = "유효기간을 입력해주세요"
                binding.tvExpiryError.visibility = View.VISIBLE
                binding.etProductDue.requestFocus()
                return@setOnClickListener
            }
            if (binding.chipFood.isChecked && expiry.isNotEmpty() && !Regex("""\d{4}-\d{2}-\d{2}""").matches(expiry)) {
                binding.tvExpiryError.text = "날짜 형식은 YYYY-MM-DD 이어야 합니다"
                binding.tvExpiryError.visibility = View.VISIBLE
                binding.etProductDue.requestFocus()
                return@setOnClickListener
            }
            
            val isAvailable = binding.btnConfirmYes.isChecked
            val isOffline = binding.btnWay1.isChecked
            val location = binding.etProductPlace.text.toString().trim()
            if (isOffline && location.isEmpty()) {
                binding.tvLocationError.text = "거래 장소를 입력해주세요"
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

            // 3) 이미지 파트
            val realUris: List<Uri> = imageUris.toList()
            val parts = if (realUris.isNotEmpty()) {
                realUris.mapIndexed { i, uri ->
                    val tmp = File(requireContext().cacheDir, "img_tissue_$i.jpg")
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
                                "firstImageUrl" to realUris.firstOrNull()?.toString() // 첫 번째 이미지 정보
                            )
                            
                            findNavController().navigate(R.id.plusCompleteFragment, bundle)
                        } else {
                            // 응답 본문 또는 result 객체가 null인 경우의 오류 처리
                            Log.e("PlusShare", "서버 응답 성공했으나, body 또는 result가 null입니다.")
                            Toast.makeText(requireContext(), "등록 결과를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val code = resp.code()
                        val errBody = resp.errorBody()?.string().orEmpty()
                        Log.e("PlusShare", "서버 오류: HTTP $code / $errBody")
                        
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}