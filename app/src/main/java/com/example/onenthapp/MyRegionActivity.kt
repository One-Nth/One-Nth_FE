package com.example.onenthapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.data.MyRegion
import com.example.onenthapp.model.MyRegionViewModel
import com.example.onenthapp.databinding.ActivityMyRegionBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles


class MyRegionActivity : AppCompatActivity() {

    private lateinit var b: ActivityMyRegionBinding
    private val vm: MyRegionViewModel by viewModels()
    private lateinit var adapter: RegionSuggestionAdapter
    private lateinit var mapView: com.kakao.vectormap.MapView // XML에 정의된 MapView
    private var kakaoMap: KakaoMap? = null
    private var markerLabelLayer: LabelLayer? = null // 마커를 관리할 레이어


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMyRegionBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.includeToolbar.btnBack.setOnClickListener { finish() }

        mapView = b.map // XML에서 MapView ID를 "map"으로 가정
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API 가 정상적으로 종료될 때 호출됨
                Log.d("MyRegionActivity", "onMapDestroy")
            }

            override fun onMapError(error: Exception) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
                Log.e("MyRegionActivity", "KakaoMap Error: ", error)
                Toast.makeText(this@MyRegionActivity, "지도 로딩 중 오류가 발생했습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                // 인증 후 API 가 정상적으로 실행될 때 호출됨
                kakaoMap = map
                markerLabelLayer = kakaoMap?.labelManager?.layer // 마커 레이어 가져오기
                // 초기 지도 설정 (예: 기본 위치 및 줌 레벨)
                val initialPosition = LatLng.from(37.5665, 126.9780) // 예: 서울 시청
                kakaoMap?.moveCamera(
                    CameraUpdateFactory.newCenterPosition(
                        initialPosition,
                        15
                    )
                ) // 줌 레벨 15

                // ViewModel에서 메인 지역 변경 관찰 시작
                observeMainRegion()
                // 현재 설정된 메인 지역이 있다면 바로 표시
                //vm.mainRegionForMap.value?.let { updateMapWithRegion(it) }
            }

            override fun getZoomLevel(): Int {
                return 15 // 기본 줌 레벨
            }
        })

        // 칩, 라벨 관찰
        vm.myRegions.observe(this) { renderChips(it) }
        // 반드시 한개의 메인 지역 보유함
//        vm.mainRegion.observe(this) { main ->
//            b.tvMapRegionLabel.visibility = if (main == null) View.GONE else View.VISIBLE
//        }
        // 주소 검색 결과
        adapter = RegionSuggestionAdapter(
            onClick = { vm.add(it.regionId) },
            onEndReached = { vm.loadMore() }
        )
        b.rvSuggestions.layoutManager = LinearLayoutManager(this)
        b.rvSuggestions.adapter = adapter

        vm.suggestions.observe(this) {
            adapter.submitList(it)
            b.cardSuggestions.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
        }

        b.searchBarEt.addTextChangedListener { s ->
            val q = s?.toString()?.trim().orEmpty()
            if (q.isEmpty()) {
                adapter.submitList(emptyList())
                b.cardSuggestions.visibility = View.GONE
            } else vm.startSearch(q)
        }

        vm.loadMyRegions()
    }

    private fun observeMainRegion() {
        vm.mainRegion.observe(this) { region ->
            region?.let {
                if (kakaoMap != null) { // 지도가 준비되었는지 확인
                    updateMapWithRegion(it)
                }
            }
        }
    }

    private fun updateMapWithRegion(region: MyRegion) {
        kakaoMap?.let { map ->
            val position = LatLng.from(127.077488598779, 37.6525550467646)

            // 1. 지도 중심 이동
            map.moveCamera(CameraUpdateFactory.newCenterPosition(position, 15)) // 줌 레벨은 적절히 조절

            // 2. 기존 마커 제거 (하나의 메인 마커만 표시한다고 가정)
            markerLabelLayer?.removeAll() // 또는 특정 ID로 마커를 관리하고 해당 마커만 제거

            // 3. 새 마커 추가
            // 마커 스타일 정의 (선택적)
            val iconStyle = LabelStyle.from(R.drawable.marker_green_72) // 커스텀 마커 아이콘 drawable
                .setAnchorPoint(0.5f, 1.0f) // 아이콘의 하단 중앙을 위치 기준으로 설정

            val styles = LabelStyles.from(iconStyle) // 단일 스타일 또는 여러 스타일 세트

            val options = LabelOptions.from(position)
                .setStyles(styles)
                // .setTexts(region.regionName) // 마커에 텍스트 표시 (선택적)
                .setRank(0) // 마커 우선순위

            markerLabelLayer?.addLabel(options)
        }
    }

    // MapView의 생명주기 관리
    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        mapView.pause()
    }

    private fun renderChips(list: List<MyRegion>) {
        b.flexSelected.removeAllViews()

        list.forEachIndexed { index, mr ->
            val chip = Chip(this, null, com.google.android.material.R.attr.chipStyle).apply {
                setChipDrawable(
                    ChipDrawable.createFromAttributes(
                        this@MyRegionActivity, null, 0, R.style.Widget_OneNth_RegionChip
                    )
                )
                text = extractDong(mr.regionName)
                isCheckable = true
                isChecked = (index == 0)      // 첫 칩 = 메인
                isCloseIconVisible = true

                // 텍스트 색상 Selector를 코드에서 직접 적용
                val textColorStateList =
                    ContextCompat.getColorStateList(this@MyRegionActivity, R.color.region_chip_text)
                setTextColor(textColorStateList) // ColorStateList 객체를 전달

                closeIcon =
                    ContextCompat.getDrawable(this@MyRegionActivity, R.drawable.btn_region_delete)
                // setCloseIconTintResource(R.color.region_chip_text)
                setOnCloseIconClickListener {
                    if (index == 0) {
                        Toast.makeText(context, "메인 지역은 삭제할 수 없어요.", Toast.LENGTH_SHORT).show()
                    } else vm.delete(mr)
                }
                setOnClickListener {
                    if (index != 0) vm.setMain(mr) // 터치 → 메인으로 승격(=첫 칩 이동)
                }
            }
            val layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val marginInDp = 18 // dp 단위의 마진 값
            val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()
            layoutParams.setMargins(0, 0, marginInPx, marginInPx) // 오른쪽, 아래 마진 설정
            chip.layoutParams = layoutParams

            b.flexSelected.addView(chip)
        }
    }

    private fun extractDong(full: String): String {
        val re = Regex("([가-힣0-9]+동)$")
        return re.find(full)?.groupValues?.get(1)
            ?: full.split(" ").lastOrNull().orEmpty()
    }
}
