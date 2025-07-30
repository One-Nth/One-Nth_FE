package com.example.onenthapp

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ScrapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrap)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        // ✅ 뒤로가기 버튼 동작 추가
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // 현재 Activity 종료 → 이전 화면으로 이동
        }

        // ✅ ViewPager 어댑터 설정
        val adapter = ScrapPagerAdapter(this)
        viewPager.adapter = adapter

        // ✅ TabLayout과 ViewPager 연결
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "N분의 1"
                1 -> "꿀팁 N분의 1"
                else -> ""
            }
        }.attach()
    }
}
