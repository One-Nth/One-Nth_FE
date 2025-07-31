package com.example.onenthapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.onenthapp.databinding.ActivityChatCheckBinding

class ChatCheckActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatCheckBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔙 뒤로가기 버튼 눌렀을 때 현재 액티비티 종료
        binding.btnLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // ✅ 거래 완료 버튼
        binding.completeButton.setOnClickListener {
            // 거래 완료 처리 로직 (예: 서버 전송 등)
            finish()
        }
    }
}
