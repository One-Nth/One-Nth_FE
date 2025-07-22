package com.example.onenthapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.onenthapp.databinding.ActivityAlarmBinding

class AlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 버튼
        binding.btnLeft.setOnClickListener {
            finish()
        }


    }
}
