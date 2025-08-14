package com.example.onenthapp.feature.chat

import android.os.Bundle
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

        // 택배 거래 버튼 클릭 시 운송장 입력란 보여주기
        binding.deliveryButton.setOnClickListener {
            binding.deliveryButton.setTextColor(getColor(R.color.main_green))
            binding.deliveryButton.setBackgroundColor(getColor(R.color.main_green_4))

            binding.inpersonButton.setTextColor(getColor(R.color.gray))
            binding.inpersonButton.setBackgroundColor(getColor(R.color.back_gray))
        }

        // 만나서 거래할래요 버튼 클릭 시
        binding.inpersonButton.setOnClickListener {
            binding.inpersonButton.setTextColor(getColor(R.color.main_green))
            binding.inpersonButton.setBackgroundColor(getColor(R.color.main_green_4))

            binding.deliveryButton.setTextColor(getColor(R.color.gray))
            binding.deliveryButton.setBackgroundColor(getColor(R.color.back_gray))
        }

        // 리뷰 버튼 클릭 시 (주석 해제 후 사용 가능)
        /*
        binding.reviewButton.setOnClickListener {
            val intent = Intent(this, WriteReviewActivity::class.java)
            startActivity(intent)
        }
        */
    }
}
