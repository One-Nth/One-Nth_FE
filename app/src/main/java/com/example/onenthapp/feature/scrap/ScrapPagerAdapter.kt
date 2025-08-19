package com.example.onenthapp.feature.scrap

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ScrapPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 2
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> ScrapProductFragment()   // 상품 스크랩
        1 -> ScrapPostFragment()      // 게시글 스크랩
        else -> ScrapPostFragment()
    }
}
