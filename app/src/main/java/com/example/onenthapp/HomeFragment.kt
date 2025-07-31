package com.example.onenthapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.onenthapp.databinding.FragmentHomeBinding
import com.example.onenthapp.databinding.ItemSearchResultBinding
import com.example.onenthapp.model.SearchResult
import com.example.onenthapp.model.SearchType
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.kakao.vectormap.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private var kakaoMapView: MapView? = null
    private var kakaoMapInstance: KakaoMap? = null
    private var lastResults: List<SearchResult> = emptyList()

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        kakaoMapView = binding.map

        // Kakao Map 초기화
        kakaoMapView?.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.d("KakaoMap", "onMapDestroy")
            }

            override fun onMapError(error: Exception) {
                Log.e("KakaoMap", "onMapError: ${error.message}", error)
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                Log.d("KakaoMap", "onMapReady")
                kakaoMapInstance = kakaoMap
            }

            override fun getPosition(): LatLng = LatLng.from(37.5665, 126.9780) // 서울 시청
            override fun getZoomLevel(): Int = 15
        })

        initBottomSheet()

        // 검색 결과 있을 경우 바로 mid로 열기
        if (lastResults.isNotEmpty()) {
            binding.bottomSheet.visibility = View.VISIBLE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        } else {
            binding.bottomSheet.visibility = View.GONE
        }

        // 드롭다운 메뉴
        val popup = PopupMenu(
            ContextThemeWrapper(requireContext(), R.style.Theme_OneNthApp),
            binding.ivArrow,
            Gravity.END
        ).apply {
            menuInflater.inflate(R.menu.menu_title_dropdown, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_n_1 -> true
                    R.id.menu_tip_n_1 -> {
                        findNavController().navigate(R.id.tipFragment)
                        true
                    }

                    else -> false
                }
            }
            setOnDismissListener {
                binding.ivArrow.animate().rotation(0f).start()
            }
        }

        // 드롭다운 화살표 클릭
        binding.ivArrow.setOnClickListener {
            binding.ivArrow.animate().rotation(180f).start()
            popup.show()
        }

        // 알림 아이콘 클릭
        binding.btNotification.setOnClickListener {
            startActivity(Intent(requireContext(), AlarmActivity::class.java))
        }

        // 검색창 엔터 이벤트
        binding.searchBarEt.setOnEditorActionListener { et, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(et.windowToken, 0)
                et.clearFocus()
                performSearch(et.text.toString())
                true
            } else false
        }

        // 탭 클릭 시 ViewModel 업데이트
        binding.tabLayoutHome.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> sharedViewModel.setCurrentHomeTab(HomeTabType.BUY)
                    1 -> sharedViewModel.setCurrentHomeTab(HomeTabType.SHARE)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    override fun onResume() {
        super.onResume()
        kakaoMapView?.resume()
        val selectedTabPosition = binding.tabLayoutHome.selectedTabPosition
        if (selectedTabPosition == 0) sharedViewModel.setCurrentHomeTab(HomeTabType.BUY)
        else sharedViewModel.setCurrentHomeTab(HomeTabType.SHARE)
    }

    override fun onPause() {
        super.onPause()
        kakaoMapView?.pause()
    }

    private fun performSearch(query: String) {
        val results = dummySearchData(query)
        lastResults = results
        val previewBinding = binding.previewCard
        results.firstOrNull()?.let {
            previewBinding.bind(it)
        }
        previewBinding.root.setOnClickListener {
            onItemClicked(results.first())
        }

        if (results.isNotEmpty()) {
            binding.bottomSheet.visibility = View.VISIBLE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        } else {
            binding.bottomSheet.visibility = View.GONE
        }
    }

    private fun dummySearchData(query: String): List<SearchResult> {
        return listOf(
            SearchResult("1", "$query 상품 A", 1000, "개", "생활용품", SearchType.BUY, listOf(android.R.drawable.btn_plus)),
            SearchResult("2", "$query 상품 B", 2000, "개", "생활용품", SearchType.BUY, listOf(android.R.drawable.btn_plus)),
            SearchResult("3", "$query 상품 C", 3000, "개", "생활용품", SearchType.BUY, listOf(android.R.drawable.btn_plus))
        )
    }

    private fun onItemClicked(item: SearchResult) {
        val action = HomeFragmentDirections.actionHomeToProductdetail()
        findNavController().navigate(action)
    }

    fun collapseSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun halfExpandBottomSheet() {
        if (::bottomSheetBehavior.isInitialized) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    fun onSearchItemSelected(result: SearchResult) {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            val action = HomeFragmentDirections.actionHomeToProductdetail()
            findNavController().navigate(action)
        } else {
            showMidPreview(result)
        }
    }

    fun showMidPreview(item: SearchResult) {
        val previewBinding = binding.previewCard
        previewBinding.bind(item)
        previewBinding.root.setOnClickListener {
            onSearchItemSelected(item)
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    private fun initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            isHideable = false
            skipCollapsed = false
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.scrollBar.isVisible = true
                        binding.midContainer.isVisible = false
                        binding.expandedContainerFragment.isVisible = false
                    }

                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        binding.scrollBar.isVisible = true
                        binding.midContainer.isVisible = true
                        binding.expandedContainerFragment.isVisible = false
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        childFragmentManager.beginTransaction()
                            .replace(
                                R.id.expandedContainerFragment,
                                SearchResultFragment.newInstance(lastResults)
                            )
                            .commitNowAllowingStateLoss()

                        binding.scrollBar.isVisible = false
                        binding.midContainer.isVisible = false
                        binding.expandedContainerFragment.isVisible = true
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }
}
