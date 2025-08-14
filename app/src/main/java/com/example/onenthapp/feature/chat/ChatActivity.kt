package com.example.onenthapp.feature.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.onenthapp.R
import com.example.onenthapp.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding

    private fun updateTabSelection(selected: Int) {
        val btnOnenth = binding.btnOnenth
        val btnTip = binding.btnTip

        if (selected == 0) {
            btnOnenth.setBackgroundResource(R.drawable.onenth_on)

            btnTip.setBackgroundResource(R.drawable.tiponenth_off)
        } else {
            btnOnenth.setBackgroundResource(R.drawable.onenth_off)

            btnTip.setBackgroundResource(R.drawable.tiponenth_on)
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