package com.example.onenthapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChatPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnenthChatFragment()
            1 -> TipChatFragment()
            else -> throw IllegalStateException("Invalid position")
        } as Fragment
    }
}
