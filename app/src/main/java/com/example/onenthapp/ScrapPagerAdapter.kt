package com.example.onenthapp

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ScrapPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ScrapTipNFragment() // ✅ N분의 1 = 상품 스크랩
            1 -> ScrapNFragment()    // ✅ 꿀팁 N분의 1 = 게시글 스크랩
            else -> ScrapTipNFragment()
        }
    }
}

