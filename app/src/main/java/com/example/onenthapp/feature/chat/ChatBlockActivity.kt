package com.example.onenthapp.feature.chat

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.onenthapp.R
import com.example.onenthapp.databinding.ActivityChatBlockBinding

class ChatBlockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBlockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            // 거래 완료 처리
            finish()
        }

        // ❌ 거래 취소 버튼
        binding.dealCancelBtn.setOnClickListener {
            // 거래 취소 처리
            finish()
        }
    }
}
