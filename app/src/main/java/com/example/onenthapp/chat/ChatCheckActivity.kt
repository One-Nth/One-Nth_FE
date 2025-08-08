package com.example.onenthapp.chat

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.onenthapp.R
import com.example.onenthapp.databinding.ActivityChatCheckBinding

class ChatCheckActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatCheckBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기
        binding.btnLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 거래 완료
        binding.completeButton.setOnClickListener {
            // 거래 완료 처리 로직
            finish()
        }

        // 택배 거래 버튼 클릭 시 운송장 입력란 보여주기
        binding.deliveryButton.setOnClickListener {
            // 운송장 입력란 보이기
            binding.trackingNumberLabel.visibility = View.VISIBLE
            binding.trackingNumberInput.visibility = View.VISIBLE

            // 버튼 스타일 바꾸기 (선택됨 표시)
            binding.deliveryButton.setTextColor(getColor(R.color.main_green))
            binding.deliveryButton.setBackgroundColor(getColor(R.color.main_green_4)) // 예: #EDF9F2

            // 만나서 거래 버튼 비활성화 스타일
            binding.inpersonButton.setTextColor(getColor(R.color.gray)) // 연한 회색
            binding.inpersonButton.setBackgroundColor(getColor(R.color.back_gray)) // 연한 배경
        }

        // 만나서 거래 버튼 클릭 시 운송장 입력란 숨기기
        binding.inpersonButton.setOnClickListener {
            binding.trackingNumberLabel.visibility = View.GONE
            binding.trackingNumberInput.visibility = View.GONE

            // 버튼 스타일 바꾸기
            binding.inpersonButton.setTextColor(getColor(R.color.main_green))
            binding.inpersonButton.setBackgroundColor(getColor(R.color.main_green_4))

            binding.deliveryButton.setTextColor(getColor(R.color.gray))
            binding.deliveryButton.setBackgroundColor(getColor(R.color.back_gray))
        }
    }

}
