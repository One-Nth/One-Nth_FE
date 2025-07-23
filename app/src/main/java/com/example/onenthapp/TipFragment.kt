package com.example.onenthapp

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.onenthapp.databinding.FragmentTipBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator

class TipFragment : Fragment() {
    private var _binding: FragmentTipBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private var sheetInitialized = false

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
                if (!sheetInitialized) {
                    initBottomSheet()
                    sheetInitialized = true
                }
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                true
            } else false
        }
    }

    private fun performSearch(query: String) {
        // 바텀시트에 검색어 표시
        binding.tvSearchKeyword.text = query
    }




    private fun initBottomSheet() {
        val sheet = binding.bottomSheet
        sheet.visibility = View.VISIBLE
        bottomSheetBehavior = BottomSheetBehavior.from(sheet).apply {
            isHideable = false
            skipCollapsed = false
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val isExpanded = newState == BottomSheetBehavior.STATE_EXPANDED
                binding.minimalContainer.visibility = if (isExpanded) View.GONE else View.VISIBLE
                binding.expandedContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
                binding.scrollBar.visibility = if (isExpanded) View.GONE else View.VISIBLE
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.toolbarSearch.setNavigationOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }
}