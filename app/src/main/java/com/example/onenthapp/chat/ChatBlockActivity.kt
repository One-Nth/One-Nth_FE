package com.example.onenthapp.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.onenthapp.R
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.databinding.ActivityChatBlockBinding
import kotlinx.coroutines.launch
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.onenthapp.data.transaction.CompleteTransactionRequest


class ChatBlockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBlockBinding
    private lateinit var roomName: String
    private var selectedProductItem: ProductItem? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomName = intent.getStringExtra("roomName") ?: ""
        Log.d("ChatBlockActivity", "roomName = $roomName")
        fetchDealConfirmationId(roomName)

        if (roomName.isNullOrBlank()) {
            showError("채팅방 정보가 없습니다. 다시 시도해주세요.")
            finish()
            return
        }

        // 🔙 뒤로가기 버튼
        binding.btnLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // ✅ 만나서 거래하기 버튼 클릭
        binding.inpersonButton.setOnClickListener {

            // 버튼 스타일: 활성화
            binding.inpersonButton.setBackgroundColor(getColor(R.color.main_green_4)) // 밝은 초록 배경
            binding.inpersonButton.setTextColor(getColor(R.color.main_green)) // 진한 초록 글자

            // 버튼 스타일: 비활성화
            binding.deliveryButton.setBackgroundColor(getColor(R.color.back_gray)) // 회색 배경
            binding.deliveryButton.setTextColor(getColor(R.color.gray)) // 회색 글자
        }

        // 🚚 택배로 거래하기 버튼 클릭
        binding.deliveryButton.setOnClickListener {

            // 버튼 스타일: 활성화
            binding.deliveryButton.setBackgroundColor(getColor(R.color.main_green_4)) // 밝은 초록 배경
            binding.deliveryButton.setTextColor(getColor(R.color.main_green)) // 진한 초록 글자

            // 버튼 스타일: 비활성화
            binding.inpersonButton.setBackgroundColor(getColor(R.color.back_gray)) // 회색 배경
            binding.inpersonButton.setTextColor(getColor(R.color.gray)) // 회색 글자
        }

        // ✅ 거래 완료 버튼
        binding.dealCompleteBtn.setOnClickListener {
            val dealDate = binding.dateInput.text.toString()
            val tradePrice = binding.priceInput.text.toString().toIntOrNull()
            val tradeCount = 1 // 혹은 사용자 입력
            val tradeType = if (binding.inpersonButton.currentTextColor == getColor(R.color.main_green)) "IN_PERSON" else "DELIVERY"

            if (dealConfirmationId == null || dealDate.isBlank() || tradePrice == null) {
                showError("모든 필드를 입력해주세요.")
                return@setOnClickListener
            }

            val request = CompleteTransactionRequest(
                dealConfirmationId = dealConfirmationId!!.toLong(),
                dealDate = dealDate,
                tradePrice = tradePrice,
                tradeCount = tradeCount,
                tradeType = tradeType
            )

            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.transactionApi.completeTransaction(roomName, request)
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.isSuccess == true) {
                            Toast.makeText(this@ChatBlockActivity, "거래가 완료되었습니다!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            showError("거래 완료 실패: ${body?.message ?: "알 수 없는 오류"}")
                        }
                    } else {
                        showError("서버 오류: ${response.code()}")
                    }
                } catch (e: Exception) {
                    showError("네트워크 오류가 발생했습니다.")
                }
            }

        }

        // ❌ 거래 취소 버튼
        binding.dealCancelBtn.setOnClickListener {
            val intent = Intent(this, CancelDealActivity::class.java)
            intent.putExtra("roomName", roomName) // 필요하다면 방 이름도 전달
            startActivity(intent)
        }


        binding.productDropdown.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2 // 오른쪽 drawable
                val drawable = binding.productDropdown.compoundDrawables[drawableEnd]

                // 터치가 drawableEnd 영역 안에 있을 경우
                if (drawable != null &&
                    event.rawX >= (binding.productDropdown.right - drawable.bounds.width() - binding.productDropdown.paddingEnd)
                ) {
                    toggleDropdown()
                    return@setOnTouchListener true
                }
            }
            false
        }

    }
    private var isDropdownVisible = false

    private fun toggleDropdown() {
        if (!isDropdownVisible) {
            // 처음 열 때만 API 호출
            if (binding.productDropdown.adapter == null || (binding.productDropdown.adapter as ArrayAdapter<*>).isEmpty) {
                fetchProductsAndSetupDropdown()
            } else {
                binding.productDropdown.showDropDown()
            }
            binding.productDropdown.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_dropbox_up, 0)
            isDropdownVisible = true
        } else {
            binding.productDropdown.dismissDropDown()
            binding.productDropdown.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_dropbox_down, 0)
            isDropdownVisible = false
        }
    }

    private var dealConfirmationId: Int? = null

    private fun fetchDealConfirmationId(roomName: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.transactionApi.getDealConfirmationForm(roomName)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.isSuccess == true && !body.result.isNullOrEmpty()) {
                        val firstItem = body.result[0] // 필요 시 선택 로직 변경
                        dealConfirmationId = firstItem.dealConfirmationid

                        // UI에 상품 보여주기 등 추가 작업 가능
                    } else {
                        showError("거래 확정 정보를 불러올 수 없습니다.")
                    }
                } else {
                    showError("서버 오류: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("네트워크 오류가 발생했습니다.")
            }
        }
    }


    private fun fetchProductsAndSetupDropdown() {
        val api = RetrofitInstance.transactionApi

        lifecycleScope.launch {
            try {
                val response = api.getAvailableProducts()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.isSuccess && responseBody.result != null) {

                        // ProductItem 리스트로 변환
                        val productItems = responseBody.result.map {
                            ProductItem(
                                name = it.itemName,
                                imageUrl = it.itemImageUrl,
                                itemId = it.itemId,
                                itemType = it.itemType
                            )
                        }

                        // DropdownProductAdapter로 세팅
                        setupProductDropdown(productItems)

                        binding.productDropdown.showDropDown() // 데이터 로드 후 드롭다운 띄우기
                    } else {
                        showError("상품 정보를 불러오는데 실패했습니다.\n${responseBody?.message ?: "알 수 없는 오류"}")
                    }
                } else {
                    showError("응답 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showError("네트워크 오류가 발생했습니다.")
            }
        }
    }

    private fun setupProductDropdown(products: List<ProductItem>) {
        val adapter = DropdownProductAdapter(this, products)
        binding.productDropdown.setAdapter(adapter)

        binding.productDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = products[position]

            // ✅ 선택된 항목 저장
            selectedProductItem = selectedItem

            // UI 업데이트
            val includedView = findViewById<View>(R.id.itemDropdownProduct)
            val itemName = includedView.findViewById<TextView>(R.id.itemName)
            val itemImage = includedView.findViewById<ImageView>(R.id.itemImage)

            includedView.visibility = View.VISIBLE
            itemName.text = selectedItem.name

            Glide.with(this)
                .load(selectedItem.imageUrl)
                .placeholder(R.drawable.box_green_border)
                .into(itemImage)

            binding.productDropdown.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, R.drawable.icon_dropbox_down, 0
            )
            isDropdownVisible = false
        }
    }


    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    }

