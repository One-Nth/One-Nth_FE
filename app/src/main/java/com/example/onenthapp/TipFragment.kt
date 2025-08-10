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

    // 현재 보드 타입을 ViewPager2에 맞춰 저장
    private var currentBoardType: String = "discount"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tabLayout = binding.tabLayoutTips
        val viewPager = binding.viewPagerTips
        val ivArrow = binding.ivArrow

        val fragments = listOf(
            DiscountTipsFragment(),
            LifeTipsFragment(),
            CafeTipsFragment()
        )
        val titles = listOf("할인 정보", "생활꿀팁", "우리동네 맛집/카페")

        viewPager.adapter = object : FragmentStateAdapter(this@TipFragment) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }
        viewPager.offscreenPageLimit = fragments.size

        // (옵션) 기본 탭을 생활꿀팁으로 시작하고 싶으면 주석 해제
        // viewPager.setCurrentItem(1, false)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()

        // ✅ ViewPager2 기준으로 현재 보드 타입 갱신
        fun updateBoardType(pos: Int) {
            currentBoardType = when (pos) {
                0 -> "discount"
                1 -> "life_tip"
                else -> "cafe"
            }
        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = updateBoardType(position)
        })
        // 초기값 세팅 (attach 직후 값 보장)
        view.post { updateBoardType(viewPager.currentItem) }

        // ▼ 드롭다운은 기존 로직 유지
        val popup = PopupMenu(
            ContextThemeWrapper(requireContext(), R.style.Theme_OneNthApp),
            ivArrow,
            Gravity.END
        ).apply {
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

        // ✅ 검색은 currentBoardType 사용
        binding.searchBarEt.setOnEditorActionListener { et, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(et.text.toString(), currentBoardType)
                true
            } else false
        }
    }

    private fun performSearch(query: String, boardType: String) {
        val intent = Intent(requireContext(), LifeTipsSearchActivity::class.java).apply {
            putExtra("query", query)
            putExtra("boardType", boardType) // "discount" | "life_tip" | "cafe"
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }
}

