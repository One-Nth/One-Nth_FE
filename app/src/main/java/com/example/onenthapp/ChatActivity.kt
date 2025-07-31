package com.example.onenthapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.onenthapp.databinding.ActivityAlarmBinding
import com.example.onenthapp.databinding.ActivityChatBinding
import com.google.android.material.tabs.TabLayoutMediator

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding

    private fun updateTabSelection(selected: Int) {
        val btnOnenth = binding.btnOnenth
        val btnTip = binding.btnTip

        if (selected == 0) {
            btnOnenth.setBackgroundResource(R.drawable.tab_button_selected)
            btnOnenth.setTextColor(resources.getColor(R.color.main_white, null))

            btnTip.setBackgroundResource(R.drawable.tab_button_unselected)
            btnTip.setTextColor(resources.getColor(R.color.main_black, null))
        } else {
            btnOnenth.setBackgroundResource(R.drawable.tab_button_unselected)
            btnOnenth.setTextColor(resources.getColor(R.color.main_black, null))

            btnTip.setBackgroundResource(R.drawable.tab_button_selected)
            btnTip.setTextColor(resources.getColor(R.color.main_white, null))
        }

        binding.viewPager.currentItem = selected
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ChatPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // 초기 선택
        updateTabSelection(0)

        binding.btnOnenth.setOnClickListener { updateTabSelection(0) }
        binding.btnTip.setOnClickListener { updateTabSelection(1) }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabSelection(position)
            }
        })

        binding.btnLeft.setOnClickListener { finish() }
    }


}