package com.example.onenthapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.onenthapp.databinding.ActivityAlarmBinding
import com.google.android.material.tabs.TabLayoutMediator

class AlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = AlarmPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "N분의 1"
                1 -> "꿀팁 N분의 1"
                else -> ""
            }
        }.attach()

        binding.btnLeft.setOnClickListener { finish() }
    }
}
