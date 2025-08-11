package com.example.onenthapp

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyPostPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MyPostProductFragment() // 첫 번째 탭 Fragment
            1 -> MyPostFragment()  // 두 번째 탭 Fragment
            else -> ScrapNFragment()
        }
    }
}
