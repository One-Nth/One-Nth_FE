package com.example.onenthapp
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import android.widget.PopupMenu
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.example.onenthapp.databinding.FragmentHomeBinding
import com.example.onenthapp.databinding.ItemSearchResultBinding
import com.example.onenthapp.model.SearchResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import androidx.navigation.fragment.findNavController
import com.example.onenthapp.model.SearchType
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private var kakaoMapView: MapView? = null // Kakao MapView 객체
    private var kakaoMapInstance: KakaoMap? = null // KakaoMap 객체

    private var lastResults: List<SearchResult> = emptyList()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        // 1) 지도 초기화
//        val fm = childFragmentManager
//        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
//            ?: MapFragment.newInstance().also {
//                fm.beginTransaction().add(R.id.map, it).commit()
//            }
//        mapFragment.getMapAsync(this)

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
        kakaoMapView = binding.map
        initBottomSheet()

        binding.btNotification.setOnClickListener {
            val intent = Intent(requireContext(), AlarmActivity::class.java)
            startActivity(intent)
        }

        // 진입 시: 결과가 있으면 mid, 없으면 숨김
        if (lastResults.isNotEmpty()) {
            initBottomSheet()
            binding.bottomSheet.visibility = View.VISIBLE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            //sheetInitialized = true
        } else binding.bottomSheet.visibility = View.GONE

        // MapView 시작
        kakaoMapView?.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API 가 정상적으로 종료될 때 호출됨
                Log.d("KakaoMap", "onMapDestroy")
                // 필요한 경우 리소스 정리
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
                // 예: val cameraUpdate = CameraUpdateFactory.newLatLng(LatLng.from(37.5665, 126.9780))
                // kakaoMap.moveCamera(cameraUpdate)
            }

            override fun getPosition(): com.kakao.vectormap.LatLng {
                // TODO("Not yet implemented")
                return com.kakao.vectormap.LatLng.from(37.5665, 126.9780) // 초기 위치 (예: 서울 시청)
            }

            override fun getZoomLevel(): Int {
                // TODO("Not yet implemented")
                return 15 // 초기 줌 레벨
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
                performSearch(et.text.toString())
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
                        //showMarkers(markerListForBuy)
                        sharedViewModel.setCurrentHomeTab(HomeTabType.BUY)
                    }
                    1 -> {
                        //showMarkers(markerListForShare)
                        sharedViewModel.setCurrentHomeTab(HomeTabType.SHARE)
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
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
        previewBinding.root.setOnClickListener { onItemClicked(results.first()) }

        // 지도 마커 갱신
        // showMarkers(results)
        // 검색 결과 리스트 갱신
        // searchAdapter.submitList(results)
        // BottomSheet 펼치기
        if(results.isNotEmpty()) {
            binding.bottomSheet.visibility = View.VISIBLE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        } else
            binding.bottomSheet.visibility = View.GONE
    }

    fun collapseSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
    fun halfExpandBottomSheet() {
        if (::bottomSheetBehavior.isInitialized) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }
    private fun onItemClicked(item: SearchResult){
        val action = HomeFragmentDirections.actionHomeToProductdetail()
        findNavController().navigate(action)
    }
    private fun dummySearchData(query: String): List<SearchResult> {
        return listOf(
            SearchResult(id = "1", title = "$query 상품 A", price = 1000, "개", category = "생활용품", type = SearchType.BUY, imageUrls = listOf(android.R.drawable.btn_plus, android.R.drawable.btn_plus, android.R.drawable.btn_plus)),
            SearchResult(id = "2", title = "$query 상품 B", price = 2000, "개", category = "생활용품", type = SearchType.BUY, imageUrls = listOf(android.R.drawable.btn_plus, android.R.drawable.btn_plus)),
            SearchResult(id = "3", title = "$query 상품 C", price = 3000, "개", category = "생활용품", type = SearchType.BUY, imageUrls = listOf(android.R.drawable.btn_plus, android.R.drawable.btn_plus))
//            SearchResult(id = "1", title = "$query 상품 A", price = 1000, image = android.R.drawable.btn_plus),
//            SearchResult(id = "2", title = "$query 상품 B", price = 2000, image = null),
//            SearchResult(id = "3", title = "$query 상품 C", price = 3000, image = android.R.drawable.btn_plus)
        )
    }
    // 바텀시트 리스트 아이템 클릭 처리
    fun onSearchItemSelected(result: SearchResult) {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            // 완전 확장 상태 → 상세로 이동
            val action = HomeFragmentDirections
                .actionHomeToProductdetail()
            //productId = result.id 나중에 상품 id 추가
            findNavController().navigate(action)
        } else {
            // mid 상태 → preview
            showMidPreview(result)
        }
    }

    fun showMidPreview(item: SearchResult) {
        // ① preview_card(include된 item_search_result.xml) 바인딩
        val previewBinding = binding.previewCard
        previewBinding.bind(item)
        previewBinding.root.setOnClickListener {
            onSearchItemSelected(item)
        }
        // ② bottom sheet을 half Expanded 로
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    private fun initBottomSheet() {
        val sheet = binding.bottomSheet
        // 뷰 보이기
//        sheet.visibility = View.VISIBLE
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