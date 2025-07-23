package com.example.onenthapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import android.widget.PopupMenu
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import androidx.navigation.fragment.findNavController


class HomeFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var sheetInitialized = false
    private var naverMap: NaverMap? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
//    private val searchAdapter = SearchAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 1) 지도 초기화
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("RestrictedApi", "DiscouragedPrivateApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 드롭다운 PopupMenu
        val popup = PopupMenu(
            ContextThemeWrapper(requireContext(), R.style.Theme_OneNthApp),
            binding.ivArrow,
            Gravity.END
        ).apply {
            menuInflater.inflate(R.menu.menu_title_dropdown, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_n_1 -> {
                        // “N분의1” 선택 처리
                        true
                    }
                    R.id.menu_tip_n_1 -> {
                        // “꿀팁 N분의1” 선택 처리
                        parentFragmentManager.beginTransaction()
                        findNavController().navigate(R.id.tipFragment)
                        true
                    }
                    else -> false
                }
            }
            setOnDismissListener {
                // 팝업 닫힐 때 화살표 복귀
                binding.ivArrow.animate().rotation(0f).start()
            }
        }
        // 화살표 회전 + 팝업 보여주기
        binding.ivArrow.setOnClickListener {
            binding.ivArrow.animate().rotation(180f).start()
            popup.show()
        }
        val searchList = binding.rvSearchResults
        searchList.layoutManager = LinearLayoutManager(requireContext())
//        searchList.adapter = searchAdapter

        // 검색창 엔터 리스너
        binding.searchBarEt.setOnEditorActionListener { et, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(et.text.toString())
                // 처음 검색할 때만 BottomSheet 보이기 + Behavior 초기화
                if (!sheetInitialized) {
                    initBottomSheet()
                    sheetInitialized = true
                }
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                true
            } else false
        }
//        // 탭 레이아웃 - 마커 교체
//        binding.tabLayoutHome.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab) {
//                when (tab.position) {
//                    0 -> showMarkers(markerListForBuy)
//                    1 -> showMarkers(markerListForShare)
//                }
//            }
//            override fun onTabUnselected(tab: TabLayout.Tab) {}
//            override fun onTabReselected(tab: TabLayout.Tab) {}
//        })

    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        // 네이버 지도 객체를 받고 필요한 설정
        this.naverMap = naverMap
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true)
        //showMarkers(emptyList())
    }

//    private fun showMarkers(data: List<T>) {
//        naverMap?.let { map ->
//            map.clear()
//            list.forEach { loc ->
//                Marker().apply {
//                    position = loc.toLatLng()
//                    captionText = loc.title
//                    this.map = map
//                }
//            }
//        }
//        naverMap?.apply {
//            clear()
//            data.forEach { loc ->
//                Marker().apply {
//                    position = loc.toLatLng()
//                    captionText = loc.title
//                    map = this@apply
//                }
//            }
//        }
//    }

    private fun performSearch(query: String) {
        // TODO: 실제 API 연동 대신 더미 데이터 생성
//        val results = dummySearchData(query)
//
//        // 지도 마커 갱신
//        showMarkers(results)
//        // 검색 결과 리스트 갱신
//        searchAdapter.submitList(results)
        // BottomSheet 펼치기
        //bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
    private fun initBottomSheet() {
        val sheet = binding.bottomSheet
        // 1) 뷰 보이기
        sheet.visibility = View.VISIBLE
        // BottomSheet 초기화
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            isHideable = false
            skipCollapsed = false
            state = BottomSheetBehavior.STATE_COLLAPSED // 초기에 341 중간 높이
        }

//        // 2) halfExpandedOffset 계산 (341dp 높이)
//        binding.root.post {
//            val parentH = binding.root.height
//            val halfH   = resources.getDimensionPixelSize(R.dimen.bottom_sheet_half_height)
//        }
        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val isExpanded = newState == BottomSheetBehavior.STATE_EXPANDED
                binding.minimalContainer.visibility  = if (isExpanded) View.GONE else View.VISIBLE
                binding.expandedContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
                binding.scrollBar.visibility         = if (isExpanded) View.GONE else View.VISIBLE
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        // RecyclerView
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
//            adapter = searchAdapter
        }

        binding.toolbarSearch.setNavigationOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
//        binding.toolbarSearch.setOnMenuItemClickListener {
//            if (it.itemId == R.id.action_close) {
//                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
//                true
//            } else false
//        }
    }
}
