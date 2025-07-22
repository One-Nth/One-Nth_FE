package com.example.onenthapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AlarmPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnenthAlarmFragment()
            1 -> TipAlarmFragment()
            else -> throw IllegalStateException("Invalid position")
        } as Fragment
    }
}
