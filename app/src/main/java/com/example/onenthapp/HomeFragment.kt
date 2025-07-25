package com.example.onenthapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import android.widget.PopupMenu
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isVisible
import com.example.onenthapp.databinding.FragmentHomeBinding
import com.example.onenthapp.databinding.ItemSearchResultBinding
import com.example.onenthapp.model.SearchResult
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var lastResults: List<SearchResult> = emptyList()

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

        // 검색창 엔터 리스너
        binding.searchBarEt.setOnEditorActionListener { et, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(et.windowToken, 0)
                et.clearFocus()
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
        val results = dummySearchData(query)
        lastResults = results
        // mid 상태: 첫 번째 아이템만 preview_card 에 바인딩
        val previewBinding: ItemSearchResultBinding = binding.previewCard
        results.firstOrNull()?.let {
//            searchResult ->
//            fun bind(item: SearchResult) {
//            }
            previewBinding.bind(it)
        }

        // 지도 마커 갱신
        // showMarkers(results)
        // 검색 결과 리스트 갱신
        // searchAdapter.submitList(results)
        // BottomSheet 펼치기
        // bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun collapseSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
    fun halfExpandBottomSheet() {
        if (::bottomSheetBehavior.isInitialized) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    private fun dummySearchData(query: String): List<SearchResult> {
        return listOf(
//            SearchResult(id = "1", title = "$query 상품 A", price = 1000, image = android.R.drawable.btn_plus),
//            SearchResult(id = "2", title = "$query 상품 B", price = 2000, image = null),
//            SearchResult(id = "3", title = "$query 상품 C", price = 3000, image = android.R.drawable.btn_plus)
        )
    }

    fun showMidPreview(item: SearchResult) {
        // ① preview_card(include된 item_search_result.xml) 바인딩
        val previewBinding = binding.previewCard
        previewBinding.bind(item)

        // ② bottom sheet을 half Expanded 로
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    private fun initBottomSheet() {
        val sheet = binding.bottomSheet
        // 뷰 보이기
        sheet.visibility = View.VISIBLE
        // BottomSheet 초기화
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            isHideable = false
            skipCollapsed = false
            state = BottomSheetBehavior.STATE_COLLAPSED // 초기에 341 중간 높이
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        // peek: 핸들만
                        binding.scrollBar.isVisible = true
                        binding.midContainer.isVisible = false
                        binding.expandedContainerFragment.isVisible = false
                    }

                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        // mid: 카드 1장
                        binding.scrollBar.isVisible = true
                        binding.midContainer.isVisible = true
                        binding.expandedContainerFragment.isVisible = false
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // full: 툴바+리스트
                        childFragmentManager.beginTransaction()
                            .replace(
                                R.id.expandedContainerFragment,
                                SearchResultFragment.newInstance(lastResults)
                            )
                            .commitNowAllowingStateLoss()
                        binding.scrollBar.isVisible = false
                        binding.midContainer.isVisible = false
                        binding.expandedContainerFragment.isVisible = true
                        // 텍스트 동기화
//                        binding.expandedContainerFragment.getFragment<>()
//                            .editText
//                            ?.setText(binding.searchBarEt.text.toString())
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }
}

fun ItemSearchResultBinding.bind(searchResult: SearchResult) {
//    this.title.text = searchResult.title // 예시 ID, 실제 ID로 변경 필요
//    this.price.text = "${searchResult.price}원"
//    searchResult.image?.let {
//        this.image.setImageResource(it) // 예시 ID, 실제 ID로 변경 필요
//    } ?: run {
//        // this.image.visibility = View.GONE 또는 기본 이미지 설정
//    }
}
