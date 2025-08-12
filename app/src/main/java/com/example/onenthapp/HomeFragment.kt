package com.example.onenthapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import android.widget.PopupMenu
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.onenthapp.databinding.FragmentHomeBinding
import com.example.onenthapp.databinding.ItemSearchResultBinding
import com.example.onenthapp.model.SearchResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.onenthapp.alarm.AlarmActivity
import com.example.onenthapp.data.GroupedMarker
import com.example.onenthapp.data.MapItemPreview
import com.example.onenthapp.data.MapRepository
import com.example.onenthapp.data.MyRegionRepository
import com.example.onenthapp.model.HomeTabType
import com.example.onenthapp.model.SearchType
import com.example.onenthapp.model.SharedViewModel
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.example.onenthapp.data.PlusRepository
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private var kakaoMapView: MapView? = null // Kakao MapView 객체
    private var kakaoMapInstance: KakaoMap? = null // KakaoMap 객체
    private val mapRepo = MapRepository()
    private val myRegionRepo = MyRegionRepository()
    private var markersLayer: LabelLayer? = null
    private var selectedLabel: com.kakao.vectormap.label.Label? = null
    private val label2Group = mutableMapOf<com.kakao.vectormap.label.Label, GroupedMarker>()
    private var lastGroups: List<GroupedMarker> = emptyList()
    private var isMidPreviewVisible = false
    // 멤버
    private val previewListAdapter by lazy { MarkerItemPreviewAdapter { onPreviewItemClick(it) } }


    // private var lastResults: List<SearchResult> = emptyList()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var currentMarkerType: String = "PURCHASEITEM"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        kakaoMapView = binding.map
        initBottomSheet()
        binding.midContainer.visibility = View.GONE
        isMidPreviewVisible = false // 상태 변수 초기화

        binding.btNotification.setOnClickListener {
            val intent = Intent(requireContext(), AlarmActivity::class.java)
            startActivity(intent)
        }

        // MapView 시작
        kakaoMapView?.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API 가 정상적으로 종료될 때 호출됨
                Log.d("KakaoMap", "onMapDestroy")
            }

            override fun onMapError(error: Exception) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
                Log.e("KakaoMap", "onMapError: ${error.message}", error)
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                // 인증 후 API 가 정상적으로 실행될 때 호출됨
                Log.d("KakaoMap", "onMapReady")
                kakaoMapInstance = kakaoMap
                // 여기에 지도 준비 완료 후 초기 설정 (예: 카메라 위치, 마커 표시 등)
                markersLayer = kakaoMapInstance?.labelManager?.layer
                kakaoMapInstance?.moveCamera(
                    CameraUpdateFactory.newCenterPosition(
                        LatLng.from(
                            37.5665,
                            126.9780
                        ), 17
                    )
                )
                // 라벨 클릭 리스너
                kakaoMapInstance?.setOnLabelClickListener { map, layer, label ->
                    handleLabelClick(label)
                    true
                }

                // 3) SharedViewModel 의 탭 변경 감지 → 마커 다시 불러오기
                sharedViewModel.currentHomeTab.observe(viewLifecycleOwner) { tab ->
                    loadMarkersByTab(tab)
                }
            }

            private fun handleLabelClick(label: Label) {
                val g = label2Group[label] ?: return

                // 1) 선택 표시: 이전 선택 복원, 현재 선택 하이라이트 + 텍스트(첫 제목)
                highlightSelectedLabel(label, g)

                // 2) 상세 미리보기 로드 (상단 1개 + 확장시 리스트)
                val ids = g.markers.map { it.id }
                viewLifecycleOwner.lifecycleScope.launch {
                    val details =
                        runCatching { mapRepo.fetchMarkerItemDetails(currentMarkerType, ids) }
                            .getOrElse { emptyList() }

                    if (details.isNotEmpty()) {
                        // 미드 프리뷰 카드 바인딩 (가장 상단 1건)
                        val top = details.first()
                        showMidPreview(top)
                        previewListAdapter.submitList(details)
                    } else {
                        // 상세 없음 → 바텀시트 최소화
                        collapseSheet()
                    }
                }
            }

            private fun highlightSelectedLabel(
                label: Label,
                group: GroupedMarker
            ) {
                val title = group.markers.firstOrNull()?.title ?: ""
                val sel = LabelStyle.from(R.drawable.marker_green_72)
                    .setAnchorPoint(0.5f, 1.0f)
                    .setTextStyles(32, Color.BLACK, 2, Color.WHITE)
                val builder = LabelTextBuilder()
                    .addTextLine(title, 0)
                label.setStyles(LabelStyles.from(sel))
                label.setTexts(builder)
                selectedLabel = label
                halfExpandBottomSheet()
            }

            private fun loadMarkersByTab(tab: HomeTabType) {
                val markerType = if (tab == HomeTabType.BUY) "PURCHASEITEM" else "SHARINGITEM"

                viewLifecycleOwner.lifecycleScope.launch {
                    // 1) 메인 지역 가져와 TV 업데이트
                    val myRegions =
                        runCatching { myRegionRepo.getMyRegions() }.getOrElse { emptyList() }
                    val main = myRegions.firstOrNull { it.main } // 첫번째 -> 메인
                    val dong = main?.regionName?.let { extractDong(it) } ?: "OO동"
                    binding.tvMyregion.text = dong

                    // 2) 마커 로딩
                    val groups = runCatching {
                        mapRepo.fetchItemMarker(
                            markerType,
                            regionId = main?.regionId
                        )
                    }
                        .getOrElse { emptyList() }

                    // 3) 지도 표시
                    markersLayer?.removeAll()
                    if (groups.isNotEmpty()) {
                        val first = groups.first()
                        kakaoMapInstance?.moveCamera(
                            CameraUpdateFactory.newCenterPosition(
                                LatLng.from(
                                    first.latitude,
                                    first.longitude
                                ), 16
                            )
                        )
                        val style =
                            LabelStyle.from(R.drawable.marker_gray_64).setAnchorPoint(0.5f, 1.0f)
                        val styles = LabelStyles.from(style)
                        groups.forEach { g ->
                            val pos = LatLng.from(g.latitude, g.longitude)
                            val label = markersLayer?.addLabel(
                                LabelOptions.from(pos).setStyles(styles).setRank(0)
                            )
                            if (label != null) label2Group[label] = g
                        }
                    }
                }
            }

            private fun extractDong(full: String): String {
                val re = Regex("([가-힣0-9]+동)$")
                return re.find(full)?.groupValues?.get(1) ?: full.split(" ").lastOrNull().orEmpty()
            }

            override fun getPosition(): com.kakao.vectormap.LatLng {
                // TODO("Not yet implemented")
                return com.kakao.vectormap.LatLng.from(37.5665, 126.9780) // 초기 위치 (예: 서울 시청)
            }

            override fun getZoomLevel(): Int {
                // TODO("Not yet implemented")
                return 17 // 초기 줌 레벨
            }
        })

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
                //performSearch(et.text.toString())
                //binding.bottomSheet.visibility = View.VISIBLE
                //bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                true
            } else false
        }

        // 탭 레이아웃 - 마커 교체
        binding.tabLayoutHome.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        sharedViewModel.setCurrentHomeTab(HomeTabType.BUY)
                    }

                    1 -> {
                        sharedViewModel.setCurrentHomeTab(HomeTabType.SHARE)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        binding.tvMyregion.setOnClickListener {
            startActivity(Intent(requireContext(), MyRegionActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        kakaoMapView?.resume() // MapView 의 resume 호출
        Log.d("KakaoMap", "onResume called, map resumed")
        // HomeFragment가 다시 활성화될 때 현재 TabLayout의 선택된 탭을 기준으로 ViewModel을 동기화
        val selectedTabPosition = binding.tabLayoutHome.selectedTabPosition
        when (selectedTabPosition) {
            0 -> sharedViewModel.setCurrentHomeTab(HomeTabType.BUY)
            1 -> sharedViewModel.setCurrentHomeTab(HomeTabType.SHARE)
        }
    }

    override fun onPause() {
        super.onPause()
        kakaoMapView?.pause() // MapView 의 pause 호출
        Log.d("KakaoMap", "onPause called, map paused")
    }
//
//    private fun performSearch(query: String) {
//        // TODO: 실제 API 연동 대신 더미 데이터 생성
//        val results = dummySearchData(query)
//        lastResults = results
//        // mid 상태: 첫 번째 아이템만 preview_card 에 바인딩
//        val previewBinding: ItemSearchResultBinding = binding.previewCard
//        results.firstOrNull()?.let {
////            searchResult ->
////            fun bind(item: SearchResult) {
////            }
//            previewBinding.bind(it)
//        }
//        previewBinding.root.setOnClickListener { onItemClicked(results.first()) }
//
//        // 지도 마커 갱신
//        // showMarkers(results)
//        // 검색 결과 리스트 갱신
//        // searchAdapter.submitList(results)
//        // BottomSheet 펼치기
//        if(results.isNotEmpty()) {
//            binding.bottomSheet.visibility = View.VISIBLE
//            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
//        } else
//            binding.bottomSheet.visibility = View.GONE
//    }

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
            SearchResult(
                id = "1",
                title = "$query 상품 A",
                price = 1000,
                "개",
                category = "생활용품",
                type = SearchType.BUY,
                imageUrls = listOf(
                    android.R.drawable.btn_plus,
                    android.R.drawable.btn_plus,
                    android.R.drawable.btn_plus
                )
            ),
            SearchResult(
                id = "2",
                title = "$query 상품 B",
                price = 2000,
                "개",
                category = "생활용품",
                type = SearchType.BUY,
                imageUrls = listOf(android.R.drawable.btn_plus, android.R.drawable.btn_plus)
            ),
            SearchResult(
                id = "3",
                title = "$query 상품 C",
                price = 3000,
                "개",
                category = "생활용품",
                type = SearchType.BUY,
                imageUrls = listOf(android.R.drawable.btn_plus, android.R.drawable.btn_plus)
            )
//            SearchResult(id = "1", title = "$query 상품 A", price = 1000, image = android.R.drawable.btn_plus),
//            SearchResult(id = "2", title = "$query 상품 B", price = 2000, image = null),
//            SearchResult(id = "3", title = "$query 상품 C", price = 3000, image = android.R.drawable.btn_plus)
        )
    }

    // 바텀시트 리스트 아이템 클릭 처리
    fun onSearchItemSelected(result: MapItemPreview) {
//        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
//            // 완전 확장 상태 → 상세로 이동
            val productId = result.id
            val action = HomeFragmentDirections.actionHomeToBuydetail(productId)
            findNavController().navigate(action)
//        } else {
//            // mid 상태 → preview
//            // showMidPreview(result)
//
//        }
    }

    fun showMidPreview(item: MapItemPreview) {
        // ① preview_card(include된 item_search_result.xml) 바인딩
        val previewBinding = binding.previewCard
        previewBinding.bind(item)
        previewBinding.root.setOnClickListener {
            onSearchItemSelected(item)
        }
        binding.midContainer.visibility = View.VISIBLE
        isMidPreviewVisible = true // 상태 변수 초기화

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
                        if(isMidPreviewVisible) binding.midContainer.isVisible = true
                        binding.expandedContainerFragment.isVisible = false
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // full: 툴바+리스트
//                        childFragmentManager.beginTransaction()
//                            .replace(
//                                R.id.expandedContainerFragment,
//                                MapItemPreviewFragment.newInstance(lastResults)
//                            )
//                            .commitNowAllowingStateLoss()
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

    private fun onPreviewItemClick(it: MapItemPreview) {
        val action = HomeFragmentDirections.actionHomeToBuydetail(it.id)
        findNavController().navigate(action)
    }
}


fun ItemSearchResultBinding.bind(item: MapItemPreview) {
    // 이미지 1~3
    ivPreview1.loadUrl(item.imageUrls.getOrNull(0))
    ivPreview2.isVisible = item.imageUrls.size >= 2
    if (ivPreview2.isVisible) ivPreview2.loadUrl(item.imageUrls[1])
    ivPreview3.isVisible = item.imageUrls.size >= 3
    if (ivPreview3.isVisible) ivPreview3.loadUrl(item.imageUrls[2])

    // 태그/상태
    tvStatus.apply {
        text = when (item.status) {
            "DEFAULT" -> "판매중"
            "IN_PROGRESS" -> "거래확정"
            "COMPLETED" -> "거래완료"
            else -> item.status
        }
        // visibility = if (item.status == "IN_PROGRESS") View.GONE else View.VISIBLE
    }
    tvCategory.text = when (item.itemCategory) {
        "HOUSEHOLD" -> "생활용품"
        "ELECTRONICS" -> "전자제품"
        "FOOD" -> "식품"
        "CLOTHING" -> "의류"
        "MISC" -> "잡화"
        else -> item.itemCategory
    }
    tvMethod.text = when (item.purchaseMethod) {
        "OFFLINE" -> "직거래"
        "ONLINE" -> "택배"
        else -> item.purchaseMethod
    }

    // 제목/가격/단위
    tvTitle.text = item.title
    tvPrice.text = item.price?.let { "${it}원" } ?: "가격 협의"
    tvUnit.text = "/ 개"  // 필요 시 서버 값으로 대체

    // 스크랩 아이콘
    btnBookmark.setImageResource(
        if (item.scraped) R.drawable.ic_bookmark_on else R.drawable.ic_bookmark_off
    )
}

fun ImageView.loadUrl(url: String?) {
    if (url.isNullOrBlank()) {
        setImageResource(R.drawable.image_placeholder_bg)
        return
    }
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.image_placeholder_bg)
        .error(R.drawable.image_placeholder_bg)
        .centerCrop()
        .into(this)
}