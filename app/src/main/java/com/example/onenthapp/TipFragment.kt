package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.onenthapp.databinding.FragmentTipBinding
import com.google.android.material.tabs.TabLayoutMediator



class TipFragment : Fragment() {
    private var _binding: FragmentTipBinding? = null
    private val binding get() = _binding!!

    // 검색용
    private var currentBoardType: String = "discount"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTipBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        val fragments = listOf(DiscountTipsFragment(), LifeTipsFragment(), CafeTipsFragment())
        val titles = listOf("할인 정보", "생활꿀팁", "우리동네 맛집/카페")

        viewPagerTips.adapter = object : FragmentStateAdapter(this@TipFragment) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }
        viewPagerTips.offscreenPageLimit = fragments.size

        TabLayoutMediator(tabLayoutTips, viewPagerTips) { tab, pos -> tab.text = titles[pos] }.attach()

        fun updateForPosition(pos: Int) {
            // 검색용 타입
            currentBoardType = when (pos) { 0 -> "discount"; 1 -> "life_tip"; else -> "cafe" }
            // ✅ FAB에 보낼 postType
            val postType = when (pos) { 0 -> "DISCOUNT"; 1 -> "LIFE_TIP"; else -> "RESTAURANT" }
            parentFragmentManager.setFragmentResult("board_tab", bundleOf("postType" to postType))
        }

        viewPagerTips.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = updateForPosition(position)
        })

        // attach 직후 초기 1회 전송
        view.post { updateForPosition(viewPagerTips.currentItem) }

        // ▼ 드롭다운 & 검색 (기존 유지)
        val popup = PopupMenu(ContextThemeWrapper(requireContext(), R.style.Theme_OneNthApp), ivArrow, Gravity.END).apply {
            menuInflater.inflate(R.menu.menu_title_dropdown, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_n_1 -> { findNavController().navigate(R.id.homeFragment); true }
                    R.id.menu_tip_n_1 -> true
                    else -> false
                }
            }
            setOnDismissListener { ivArrow.animate().rotation(0f).start() }
        }
        ivArrow.setOnClickListener { ivArrow.animate().rotation(180f).start(); popup.show() }

        searchBarEt.setOnEditorActionListener { et, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(et.text.toString(), currentBoardType); true
            } else false
        }
    }

    private fun performSearch(query: String, boardType: String) {
        startActivity(Intent(requireContext(), LifeTipsSearchActivity::class.java).apply {
            putExtra("query", query)
            putExtra("boardType", boardType)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
    }
}
