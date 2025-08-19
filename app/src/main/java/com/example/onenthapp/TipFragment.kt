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
import com.example.onenthapp.feature.alarm.AlarmActivity
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
            // 검색용 타입만 업데이트 (FAB은 클릭 시점에 직접 확인)
            currentBoardType = when (pos) { 0 -> "discount"; 1 -> "life_tip"; else -> "cafe" }
        }

        viewPagerTips.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = updateForPosition(position)
        })

        // attach 직후 초기 1회 실행
        view.postDelayed({ updateForPosition(viewPagerTips.currentItem) }, 100)

        // 복잡한 리스너 제거 - 대신 직접 메서드 호출 방식 사용

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
                // 검색 시점에서 현재 탭 위치 직접 확인
                val currentPosition = viewPagerTips.currentItem
                val realBoardType = when (currentPosition) { 
                    0 -> "discount"
                    1 -> "life_tip" 
                    else -> "cafe"
                }
                performSearch(et.text.toString(), realBoardType); true
            } else false
        }

        btNotification.setOnClickListener {
            val intent = Intent(requireContext(), AlarmActivity::class.java)
            intent.putExtra("alarm_type", "TIP")  // 선택 사항: 어떤 탭에서 왔는지 전달
            startActivity(intent)
        }

    }

    // MainActivity에서 현재 탭 위치를 가져오는 메서드
    fun getCurrentTabPosition(): Int {
        return try {
            if (_binding != null) {
                binding.viewPagerTips.currentItem
            } else {
                1 // 기본값: 생활꿀팁
            }
        } catch (e: Exception) {
            1 // 오류 발생 시 기본값
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
