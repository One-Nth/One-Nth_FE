package com.example.onenthapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.example.onenthapp.databinding.FragmentPlusShareBinding
import androidx.navigation.fragment.findNavController
import com.example.onenthapp.data.PlusRepository
import com.example.onenthapp.data.ShareRequest
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlusShareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnProductSubmit.setOnClickListener {
            // 1) 폼 값 읽기
            val title = binding.etProductName.text.toString()
            val quantity = binding.etProductNum.text.toString().toIntOrNull() ?: 1
            val priceStr = binding.etProductCost.text.toString().trim()
            val price = priceStr.toIntOrNull() ?: run {
                Toast.makeText(requireContext(), "가격을 숫자로 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val category = binding.cgCategoryShare.checkedChipId
            val expirationDate = binding.etProductDue.text.toString()
            val isAvailable = binding.btnConfirmYes.isChecked
            // 2) 요청 객체 생성
            val req = ShareRequest(
                title = title,
                quantity = quantity,
                price = price,
                itemCategory = "ELECTRONICS",               // 실제 선택값으로 교체
                expirationDate = expirationDate,
                isAvailable = isAvailable,
                purchaseMethod = if (binding.btnWay1.isChecked) "OFFLINE" else "ONLINE",
                regionId = 1,
                tags = listOf("#예시")               // 실제 태그 파싱 로직으로 교체
            )
            // JSON → RequestBody
            val json = Gson().toJson(req)
            val dataPart = json.toRequestBody("application/json".toMediaType())

            // 3) 이미지 파트 (실제 업로드 미구현 상태라 dummy 이미지 하나 강제)
            val realUris: List<Uri> = emptyList() // TODO: 실제 Uri 리스트
            val parts = if (realUris.isEmpty()) {
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
            binding.includeToolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            setupToggleButton1()
            setupToggleButton2()

        }
    }
    private fun setupToggleButton1() {
        binding.btnConfirmYes.setOnClickListener {
            binding.btnConfirmYes.isChecked = true
            binding.btnConfirmNoCancel.isChecked = false
        }
        binding.btnConfirmNoCancel.setOnClickListener {
            binding.btnConfirmNoCancel.isChecked = true
            binding.btnConfirmYes.isChecked = false
        }
    }
    private fun setupToggleButton2() {
        binding.btnWay1.setOnClickListener {
            binding.btnWay1.isChecked = true
            binding.btnWay2.isChecked = false
        }
        binding.btnWay2.setOnClickListener {
            binding.btnWay2.isChecked = true
            binding.btnWay1.isChecked = false
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}