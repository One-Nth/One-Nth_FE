package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.onenthapp.databinding.FragmentTipBinding
import com.google.android.material.tabs.TabLayoutMediator

class TipFragment : Fragment() {
    private var _binding: FragmentTipBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()

        val popup = PopupMenu(
            ContextThemeWrapper(requireContext(), R.style.Theme_OneNthApp),
            ivArrow,
            Gravity.END
        ).apply {
            menuInflater.inflate(R.menu.menu_title_dropdown, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_n_1 -> {
                        findNavController().navigate(R.id.homeFragment)
                        true
                    }
                    R.id.menu_tip_n_1 -> true
                    else -> false
                }
            }
            setOnDismissListener {
                ivArrow.animate().rotation(0f).start()
            }
        }

        ivArrow.setOnClickListener {
            ivArrow.animate().rotation(180f).start()
            popup.show()
        }

        binding.searchBarEt.setOnEditorActionListener { et, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(et.text.toString())
                true
            } else false
        }
    }

    private fun performSearch(query: String) {
        val currentTabPosition = binding.tabLayoutTips.selectedTabPosition

        // 게시판 구분
        val boardType = when (currentTabPosition) {
            0 -> "discount"   // 할인 정보
            1 -> "life_tip"   // 생활꿀팁
            2 -> "cafe"       // 우리동네 맛집/카페
            else -> "unknown"
        }

        val intent = Intent(requireContext(), LifeTipsSearchActivity::class.java).apply {
            putExtra("query", query)
            putExtra("boardType", boardType)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }



}