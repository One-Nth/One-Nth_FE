package com.example.onenthapp.feature.board

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.onenthapp.R
import com.example.onenthapp.data.map.*
import com.example.onenthapp.databinding.FragmentTipsDiscounttipsBinding
import com.example.onenthapp.databinding.ItemMarkerDetailBinding
import com.example.onenthapp.feature.map.MyRegionActivity
import com.example.onenthapp.RetrofitInstance
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdateFactory
import kotlinx.coroutines.launch
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kakao.vectormap.label.LabelTextBuilder

class DiscountTipsFragment : Fragment() {

    private var _binding: FragmentTipsDiscounttipsBinding? = null
    private val binding get() = _binding!!

    private var kakaoMapInstance: KakaoMap? = null
    private var markersLayer: LabelLayer? = null
    private var selectedLabel: Label? = null
    private val label2Group = mutableMapOf<Label, GroupedPostMarker>()
    private var lastGroups: List<GroupedPostMarker> = emptyList()
    private var defaultStyles: LabelStyles? = null
    private var selectedStyles: LabelStyles? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private val scrapState = mutableMapOf<Long, Boolean>()  // 북마크 상태 관리

    private val mapRepo = MapRepository()
    private val myRegionRepo = MyRegionRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTipsDiscounttipsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupMapView()
        loadCurrentRegion()
    }

    private fun setupViews() {
        // 바텀시트 초기화
        initBottomSheet()

        // 지역 변경하기 텍스트뷰 클릭 리스너
        binding.tvMyregion.setOnClickListener {
            val intent = Intent(context, MyRegionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initBottomSheet() {
        val sheet = binding.bottomSheet
        // 뷰 보이기
        sheet.visibility = View.VISIBLE
        // BottomSheet 초기화
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            isHideable = false
            skipCollapsed = false
            state = BottomSheetBehavior.STATE_COLLAPSED // 초기에 collapsed 상태
            isDraggable = false
        }
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        // peek: 핸들만
                        binding.scrollBar.visibility = View.VISIBLE
                        binding.midContainer.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        // mid: 카드 1장
                        binding.scrollBar.visibility = View.VISIBLE
                        binding.midContainer.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // full: 전체 확장
                        binding.scrollBar.visibility = View.GONE
                        binding.midContainer.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 슬라이드 중 상태 관리
            }
        })
    }

    private fun setupMapView() {
        binding.mapViewDisplay.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.d("DiscountTipsFragment", "onMapDestroy")
            }

            override fun onMapError(error: Exception) {
                Log.e("DiscountTipsFragment", "onMapError: ${error.message}", error)
                Toast.makeText(context, "지도 로딩 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                Log.d("DiscountTipsFragment", "KakaoMap is ready for DiscountTips.")
                kakaoMapInstance = kakaoMap
                markersLayer = kakaoMapInstance?.labelManager?.layer

                // 마커 스타일 설정 (HomeFragment와 동일)
                setupMarkerStyles()

                // 라벨 클릭 리스너
                kakaoMapInstance?.setOnLabelClickListener { map, layer, label ->
                    handleLabelClick(label)
                    true
                }

                // 초기 마커 로드
                loadDiscountMarkers()
            }
        })
    }

    private fun setupMarkerStyles() {
        kakaoMapInstance?.labelManager?.let { lm ->
            defaultStyles = lm.addLabelStyles(
                LabelStyles.from(
                    LabelStyle.from(R.drawable.marker_gray_64)
                        .setAnchorPoint(0.5f, 1.0f) // 마커 아래에 텍스트 표시
                        .setApplyDpScale(false)
                )
            )
            selectedStyles = lm.addLabelStyles(
                LabelStyles.from(
                    LabelStyle.from(R.drawable.marker_green_72)
                        .setAnchorPoint(0.5f, 1.0f) // 마커 아래에 텍스트 표시
                        .setTextStyles(32, Color.BLACK, 2, Color.WHITE) // 텍스트 스타일 설정
                        .setApplyDpScale(false)
                )
            )
        }
    }

    private fun loadCurrentRegion() {
        lifecycleScope.launch {
            try {
                val myRegions = myRegionRepo.getMyRegions()
                val mainRegion = myRegions.find { it.main }
                mainRegion?.let { region ->
                    binding.tvMyregion.text = extractDong(region.regionName)
                    
                    // 지역 중심 좌표 가져오기
                    val centerResult = myRegionRepo.getRegionCenter(region.regionName)
                    centerResult?.let { center ->
                        updateMapCenter(center.latitude, center.longitude)
                        loadDiscountMarkers(region.regionId)
                    }
                }
            } catch (e: Exception) {
                Log.e("DiscountTipsFragment", "지역 정보 로드 실패", e)
            }
        }
    }

    private fun updateMapCenter(latitude: Double, longitude: Double) {
        val map = kakaoMapInstance ?: return
        val pos = LatLng.from(latitude, longitude)
        map.moveCamera(CameraUpdateFactory.newCenterPosition(pos, 15))
    }

    private fun loadDiscountMarkers(regionId: Long? = null) {
        lifecycleScope.launch {
            try {
                val groups = mapRepo.fetchPostMarkers("DISCOUNT", regionId)
                if (groups.isNotEmpty()) {
                    lastGroups = groups
                    displayMarkers(groups)
                }
            } catch (e: Exception) {
                Log.e("DiscountTipsFragment", "마커 로드 실패", e)
                Toast.makeText(context, "마커를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayMarkers(groups: List<GroupedPostMarker>) {
        val layer = markersLayer ?: return
        layer.removeAll()
        label2Group.clear()

        groups.forEach { group ->
            val pos = LatLng.from(group.latitude, group.longitude)
            val options = LabelOptions.from(pos)
                .setStyles(defaultStyles!!)
                .setRank(0)

            val label = layer.addLabel(options)
            label2Group[label] = group
        }
    }

    private fun handleLabelClick(label: Label) {
        val group = label2Group[label] ?: return

        // 이전 선택된 마커를 기본 스타일로 변경
        selectedLabel?.let { prevLabel ->
            prevLabel.setStyles(defaultStyles!!)
            prevLabel.changeText(LabelTextBuilder()) // 텍스트 제거
        }

        // 현재 선택된 마커를 활성화 스타일로 변경하고 placeName 표시
        highlightSelectedLabel(label, group)

        // 바텀시트에 마커 상세 정보 표시
        showMarkerDetails(group)
    }

    private fun highlightSelectedLabel(label: Label, group: GroupedPostMarker) {
        // 이전 선택 라벨 원복
        selectedLabel?.let { prev ->
            defaultStyles?.let { prev.changeStyles(it) }
            prev.changeText(LabelTextBuilder()) // 텍스트 제거
        }
        
        // placeName을 라벨에 표시 (마커 아래에)
        lifecycleScope.launch {
            try {
                val postIds = group.markers.map { it.id }
                val details = mapRepo.fetchPostMarkerDetails("DISCOUNT", postIds)
                if (details.isNotEmpty()) {
                    val name = details.first().placeName
                    val builder = LabelTextBuilder().addTextLine(name, 0)
                    selectedStyles?.let { styles -> 
                        label.changeStylesAndText(styles, builder) 
                    }
                }
            } catch (e: Exception) {
                Log.e("DiscountTipsFragment", "placeName 로드 실패", e)
            }
        }
        
        selectedLabel = label
        
        // 바텀시트를 half expanded 상태로
        halfExpandBottomSheet()
    }

    fun collapseSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun halfExpandBottomSheet() {
        if (::bottomSheetBehavior.isInitialized) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    private fun showMarkerDetails(group: GroupedPostMarker) {
        lifecycleScope.launch {
            try {
                val postIds = group.markers.map { it.id }
                val details = mapRepo.fetchPostMarkerDetails("DISCOUNT", postIds)
                if (details.isNotEmpty()) {
                    // 첫 번째 게시글 정보를 바인딩
                    val firstPost = details.first()
                    bindPostPreview(firstPost)
                    
                    // 바텀시트를 half expanded 상태로
                    halfExpandBottomSheet()
                }
            } catch (e: Exception) {
                Log.e("DiscountTipsFragment", "마커 상세 정보 로드 실패", e)
                Toast.makeText(context, "상세 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindPostPreview(postPreview: PostMarkerPreview) {
        val postPreviewCard = binding.postPreviewCard
        val itemBinding = ItemMarkerDetailBinding.bind(postPreviewCard.root)
        
        // 마커를 누를 때마다 항상 서버 값으로 scrapState 갱신
        scrapState[postPreview.id] = postPreview.scraped
        
        // 서버 값으로 초기 상태 설정
        val initialScraped = postPreview.scraped
        
        itemBinding.tvPlaceName.text = postPreview.placeName
        itemBinding.tvTitle.text = postPreview.title
        itemBinding.tvAddress.text = postPreview.address
        
        // 북마크 상태 관리
        var current = initialScraped
        fun renderIcon() {
            itemBinding.btnBookmark.setImageResource(
                if (current) R.drawable.ic_bookmini_on else R.drawable.ic_bookmini_off
            )
        }
        renderIcon()
        
        itemBinding.tvTime.text = getRelativeTimeString(postPreview.createdAt)
        
        // 북마크 클릭 리스너
        itemBinding.btnBookmark.setOnClickListener {
            val before = current
            current = !before  // 1) 낙관적 토글
            renderIcon()
            
            // 2) 서버 호출 후 결과에 따라 확정/롤백
            toggleBookmark(postPreview.id, before) { success ->
                if (success) {
                    // 성공 시 scrapState에 저장
                    scrapState[postPreview.id] = current
                } else {
                    // 실패 시 롤백
                    current = before
                    renderIcon()
                }
            }
        }
        
        // 게시글 클릭 시 상세 화면으로 이동
        postPreviewCard.root.setOnClickListener {
            val intent = Intent(requireContext(), LifeTipsDetailActivity::class.java).apply {
                putExtra("postId", postPreview.id)
                putExtra("scrapped", current)  // 현재 UI 상태 사용
            }
            startActivity(intent)
        }
        
        // 스크롤바 클릭 시 바텀시트 닫기
        binding.scrollBar.setOnClickListener {
            collapseSheet()
        }
    }

    private fun getRelativeTimeString(createdAt: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val createdDate = inputFormat.parse(createdAt)
            val currentDate = java.util.Date()
            
            val diffInMillis = currentDate.time - (createdDate?.time ?: 0)
            val diffInMinutes = diffInMillis / (1000 * 60)
            val diffInHours = diffInMinutes / 60
            val diffInDays = diffInHours / 24
            
            when {
                diffInMinutes < 1 -> "방금전"
                diffInMinutes < 60 -> "${diffInMinutes}분전"
                diffInHours < 24 -> "${diffInHours}시간전"
                diffInDays < 7 -> "${diffInDays}일전"
                else -> {
                    val outputFormat = java.text.SimpleDateFormat("MM.dd", java.util.Locale.getDefault())
                    createdDate?.let { outputFormat.format(it) } ?: createdAt
                }
            }
        } catch (e: Exception) {
            createdAt
        }
    }

    private fun extractDong(full: String?): String? {
        if (full.isNullOrBlank()) return null

        // 구분자 정리 후 토큰화
        val tokens = full.replace(",", " ")
            .replace("·", " ")
            .split(" ")
            .filter { it.isNotBlank() }

        // 말단 행정단위(동/가/읍/면/리) 우선 탐색
        val suffixes = listOf("동", "가", "읍", "면", "리")
        return tokens.asReversed().firstOrNull { t -> suffixes.any { t.endsWith(it) } }
            ?: tokens.lastOrNull() // 혹시 못 찾으면 마지막 토큰
    }

    // 북마크 토글 함수
    private fun toggleBookmark(id: Long, before: Boolean, onDone: (Boolean) -> Unit) {
        Log.d("DiscountTipsFragment", "toggleBookmark 시작: postId=$id, before=$before")
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.notificationboardApi
                
                if (before) {
                    // 스크랩 취소
                    Log.d("DiscountTipsFragment", "스크랩 취소 API 호출: postId=$id")
                    val res = api.unscrapPost(id.toInt())
                    Log.d("DiscountTipsFragment", "스크랩 취소 응답: isSuccessful=${res.isSuccessful}, body=${res.body()}")
                    
                    if (res.isSuccessful && res.body()?.result?.isSuccess == true) {
                        onDone(true)
                        Toast.makeText(context, "스크랩을 취소했어요.", Toast.LENGTH_SHORT).show()
                    } else {
                        onDone(false)
                        Toast.makeText(context, res.body()?.message ?: "스크랩 취소 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 스크랩 등록
                    Log.d("DiscountTipsFragment", "스크랩 등록 API 호출: postId=$id")
                    val res = api.scrapPost(id.toInt())
                    Log.d("DiscountTipsFragment", "스크랩 등록 응답: isSuccessful=${res.isSuccessful}, body=${res.body()}")
                    
                    if (res.isSuccessful && res.body()?.result?.isSuccess == true) {
                        onDone(true)
                        Toast.makeText(context, "스크랩했어요.", Toast.LENGTH_SHORT).show()
                    } else {
                        onDone(false)
                        Toast.makeText(context, res.body()?.message ?: "스크랩 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("DiscountTipsFragment", "북마크 토글 중 예외 발생", e)
                onDone(false)
                Toast.makeText(context, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (_binding != null) {
            // ViewPager 변경 시 지도 완전 정리
            markersLayer?.removeAll()
            selectedLabel = null
            label2Group.clear()
            binding.mapViewDisplay.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            binding.mapViewDisplay.resume()
            // 지역 정보 새로고침 및 마커 재로드
            loadCurrentRegion()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (_binding != null && binding.mapViewDisplay.isAttachedToWindow) {
            // 마커 레이어 정리
            markersLayer?.removeAll()
            // 지도 완전 종료
            binding.mapViewDisplay.finish()
        }
        _binding = null
        kakaoMapInstance = null
        markersLayer = null
        selectedLabel = null
        label2Group.clear()
    }
}
