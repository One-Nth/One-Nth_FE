//package com.example.onenthapp
//
//import androidx.appcompat.app.AppCompatActivity
//import androidx.fragment.app.Fragment
//import androidx.viewpager2.adapter.FragmentStateAdapter
//
//class ScrapPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
//    override fun getItemCount(): Int = 2
//
//    override fun createFragment(position: Int): Fragment {
//        return when (position) {
//            0 -> ScrapProductFragment() // ✅ N분의 1 = 상품 스크랩
//            1 -> ScrapPostFragment()    // ✅ 꿀팁 N분의 1 = 게시글 스크랩
//            else -> ScrapProductFragment()
//        }
//    }
//}


package com.example.onenthapp

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
