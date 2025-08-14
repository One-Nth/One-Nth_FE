package com.example.onenthapp.chat

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.onenthapp.R
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.data.transaction.DealCompletionRequest
import com.example.onenthapp.databinding.ActivityChatCheckBinding
import kotlinx.coroutines.launch

class ChatCheckActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatCheckBinding

    private lateinit var roomName: String
    private var selectedProductItem: ProductItem? = null
    private var isDropdownVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomName = intent.getStringExtra("roomName") ?: ""
        if(roomName.isBlank()){
            Toast.makeText(this, "채팅방 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 뒤로가기
        binding.btnLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 거래 유형 버튼 클릭
        binding.deliveryButton.setOnClickListener {
            selectTradeType("DELIVERY")
        }

        binding.inpersonButton.setOnClickListener {
            selectTradeType("IN_PERSON")
        }

        // 초기 거래 타입은 IN_PERSON
        selectTradeType("IN_PERSON")

        // 상품 드롭다운 터치 시 토글
        binding.productDropdown.setOnTouchListener { _, event ->
            if(event.action == MotionEvent.ACTION_UP){
                val drawableEnd = 2
                val drawable = binding.productDropdown.compoundDrawables[drawableEnd]
                if(drawable != null &&
                    event.rawX >= (binding.productDropdown.right - drawable.bounds.width() - binding.productDropdown.paddingEnd)
                ){
                    toggleDropdown()
                    return@setOnTouchListener true
                }
            }
            false
        }

        // 거래 확정 버튼 클릭 (아래에 버튼 아이디 confirmDealBtn 으로 가정)
        binding.completeButton.setOnClickListener {
            confirmDeal()
        }
    }

    private fun selectTradeType(type: String){
        when(type){
            "IN_PERSON" -> {
                binding.inpersonButton.setTextColor(getColor(R.color.main_green))
                binding.inpersonButton.setBackgroundColor(getColor(R.color.main_green_4))

                binding.deliveryButton.setTextColor(getColor(R.color.gray))
                binding.deliveryButton.setBackgroundColor(getColor(R.color.back_gray))
            }
            "DELIVERY" -> {
                binding.deliveryButton.setTextColor(getColor(R.color.main_green))
                binding.deliveryButton.setBackgroundColor(getColor(R.color.main_green_4))

                binding.inpersonButton.setTextColor(getColor(R.color.gray))
                binding.inpersonButton.setBackgroundColor(getColor(R.color.back_gray))
            }
        }
    }

    private fun toggleDropdown(){
        if(!isDropdownVisible){
            if(binding.productDropdown.adapter == null || (binding.productDropdown.adapter as ArrayAdapter<*>).isEmpty){
                fetchProductsAndSetupDropdown()
            } else {
                binding.productDropdown.showDropDown()
            }
            binding.productDropdown.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_dropbox_up,0)
            isDropdownVisible = true
        } else {
            binding.productDropdown.dismissDropDown()
            binding.productDropdown.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_dropbox_down,0)
            isDropdownVisible = false
        }
    }

    private fun fetchProductsAndSetupDropdown(){
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.transactionApi.getAvailableProducts()
                if(response.isSuccessful){
                    val body = response.body()
                    if(body != null && body.isSuccess && body.result != null){
                        val products = body.result.map {
                            ProductItem(it.itemName, it.itemImageUrl, it.itemId, it.itemType)
                        }
                        setupProductDropdown(products)
                        binding.productDropdown.showDropDown()
                    } else {
                        showError("상품 정보를 불러오는데 실패했습니다.")
                    }
                } else {
                    showError("서버 오류: ${response.code()}")
                }
            } catch(e: Exception){
                e.printStackTrace()
                showError("네트워크 오류가 발생했습니다.")
            }
        }
    }

    private fun setupProductDropdown(products: List<ProductItem>){
        val adapter = DropdownProductAdapter(this, products)
        binding.productDropdown.setAdapter(adapter)

        binding.productDropdown.setOnItemClickListener { _, _, position, _ ->
            val selected = products[position]
            selectedProductItem = selected

            // 상품 정보 UI (R.id.itemDropdownProduct 내부 아이템)
            val includedView = findViewById<View>(R.id.itemDropdownProduct)
            val itemName = includedView.findViewById<TextView>(R.id.itemName)
            val itemImage = includedView.findViewById<ImageView>(R.id.itemImage)

            includedView.visibility = View.VISIBLE
            itemName.text = selected.name

            Glide.with(this)
                .load(selected.imageUrl)
                .placeholder(R.drawable.box_green_border)
                .into(itemImage)

            binding.productDropdown.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_dropbox_down,0)
            isDropdownVisible = false
        }
    }

    private fun extractOtherMemberId(roomName: String, myId: String): String? {
        val parts = roomName.split("-")
        if(parts.size < 3) return null

        // 예: "12-34-TIP_SHARE" 에서 상대멤버아이디는 34
        return parts.firstOrNull { it != myId && it != "TIP_SHARE" }
    }


    private fun confirmDeal(){
        val product = selectedProductItem
        if(product == null){
            showError("상품을 선택해주세요.")
            return
        }

        val dealDate = binding.dateInput.text.toString()
        if(dealDate.isBlank()){
            showError("거래 날짜를 입력해주세요.")
            return
        }

        val purchasePrice = binding.priceInput.text.toString().toIntOrNull()
        val originalPrice = binding.originalpriceInput.text.toString().toIntOrNull()
        if(purchasePrice == null || originalPrice == null){
            showError("가격을 정확히 입력해주세요.")
            return
        }

        val tradeType = if(binding.inpersonButton.currentTextColor == getColor(R.color.main_green)) "IN_PERSON" else "DELIVERY"

        // 내 아이디: JWT 토큰에서 추출하거나, 앱 내 저장된 유저 ID 가져오기 (예: myId 변수)
        val myId = "12" // 예시, 실제로는 로그인된 내 아이디 넣기

        val otherMemberIdStr = extractOtherMemberId(roomName, myId)
        val otherMemberId = otherMemberIdStr?.toIntOrNull()
        if(otherMemberId == null){
            showError("상대 멤버 아이디를 숫자로 변환하지 못했습니다.")
            return
        }


        val request = DealCompletionRequest(
            itemId = product.itemId,
            itemType = product.itemType,
            dealDate = dealDate,
            tradeType = tradeType,
            purchasePrice = purchasePrice,
            originalPrice = originalPrice// 여기 추가
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.transactionApi.confirmationTransaction(roomName, request)
                if(response.isSuccessful){
                    val body = response.body()
                    if(body?.isSuccess == true){
                        Toast.makeText(this@ChatCheckActivity, "거래가 성공적으로 확정되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        showError("거래 확정 실패: ${body?.message ?: "알 수 없는 오류"}")
                    }
                } else {
                    showError("서버 오류: ${response.code()}")
                }
            } catch(e: Exception){
                showError("네트워크 오류가 발생했습니다.")
            }
        }
    }


    private fun showError(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
