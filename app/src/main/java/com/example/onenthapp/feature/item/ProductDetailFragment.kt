package com.example.onenthapp.feature.item

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.onenthapp.R
import com.example.onenthapp.databinding.FragmentProductDetailBinding
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private var mapView: MapView? = null
    private var kakaoMapInstance: KakaoMap? = null // onMapReady에서 받을 KakaoMap 객체
    private val PRODUCT_LATITUDE = 37.394660 // 서울 시청 예시 위도
    private val PRODUCT_LONGITUDE = 127.111182 // 서울 시청 예시 경도
    private val PRODUCT_MARKER_NAME = "거래 장소"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        mapView = binding.locationMap
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         val dotsIndicator = binding.dotsIndicator // XML의 DotsIndicator ID
         dotsIndicator.attachTo(binding.viewpagerImages)
         startMap()
    }

    private fun startMap() {
        mapView?.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API 가 정상적으로 종료될 때 호출됨
                Log.d("ProductDetailFragment", "MapLifeCycleCallback: onMapDestroy")
            }

            override fun onMapError(error: Exception) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
                Log.e("ProductDetailFragment", "MapLifeCycleCallback: onMapError: ${error.message}", error)
                // 사용자에게 오류 메시지 표시 등의 처리
                // 예: Toast.makeText(context, "지도 로딩 실패: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                // 인증 후 API가 정상적으로 실행될 때 호출됨
                Log.d("ProductDetailFragment", "KakaoMapReadyCallback: onMapReady")
                kakaoMapInstance = kakaoMap
                // 여기에 지도 관련 설정 및 마커 추가 로직 호출
                setupMapAndAddMarker()
            }

            private fun setupMapAndAddMarker() {
                val currentKakaoMap = kakaoMapInstance ?: return // KakaoMap 객체가 없으면 아무것도 하지 않음

                // 마커(Label) 추가
                addMarkerToMap(currentKakaoMap, LatLng.from(PRODUCT_LATITUDE, PRODUCT_LONGITUDE))
            }

            private fun addMarkerToMap(
                kakaoMap: KakaoMap,
                position: LatLng
            ) {       val manager = kakaoMap.labelManager ?: return
                val fullyOpaqueBlack = 0xFF000000.toInt()
                val fullyOpaqueWhite = 0xFFFFFFFF.toInt() // .toInt()는 Long을 Int로 변환 (Kotlin)
                // 1. LabelStyles 생성 (아이콘 + 텍스트 스타일)
                val styles = manager.addLabelStyles(
                    LabelStyles.from(
                        LabelStyle.from(R.drawable.marker_green_72) // 프로젝트에 마커 아이콘(ic_map_marker_default.png)이 있다고 가정
                            .setTextStyles(
                                35, // 텍스트 크기 (sp 단위가 아님, 픽셀 크기)
                                fullyOpaqueBlack, // 텍스트 색상
                                2, // 테두리 두께
                                fullyOpaqueWhite // 테두리 색상
                            )
                            .setAnchorPoint(0.5f, 1.0f) // 아이콘의 하단 중앙을 좌표에 맞춤
                            .setApplyDpScale(false)
                    )
                )

                // 2. LabelOptions 생성
                val options = LabelOptions.from(position)
                    .setStyles(styles)
//                    .setTexts(PRODUCT_MARKER_NAME, PRODUCT_MARKER_DETAIL_TEXT) // 여러 줄 텍스트 가능
                val labelTextBuilder = LabelTextBuilder()
                    .addTextLine(PRODUCT_MARKER_NAME, 0)
                options.setTexts(labelTextBuilder)
                // 3. LabelLayer 가져오기 (기본 레이어 사용)
                val layer = manager.layer

                // 4. LabelLayer에 LabelOptions을 넣어 Label 생성하기
                val label = layer?.addLabel(options)

                if (label != null) {
                    Log.d("ProductDetailFragment", "Marker(Label) added: ${label.texts}")
                } else {
                    Log.e("ProductDetailFragment", "Failed to add marker (Label)")
                }

            }

            // --- 지도 초기화 시 다양한 설정 (공식 문서 참고) ---
            override fun getPosition(): LatLng {
                // 지도 시작 시 위치 좌표
                return LatLng.from(PRODUCT_LATITUDE, PRODUCT_LONGITUDE)
            }

            override fun getZoomLevel(): Int {
                return 18 // 예시로 16 레벨 (상세보기에 적절한 수준)
            }
        })
    }

    // --- MapView 생명주기 관리 (Fragment의 생명주기에 맞춰 호출 - 공식 문서 권장) ---
    override fun onResume() {
        super.onResume()
        mapView?.resume() // MapView의 resume 호출 (필수)
        Log.d("ProductDetailFragment", "MapView resumed")
    }

    override fun onPause() {
        super.onPause()
        mapView?.pause() // MapView의 pause 호출 (필수)
        Log.d("ProductDetailFragment", "MapView paused")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // ViewPager2의 어댑터를 null로 설정하여 메모리 누수 방지 고려
        // binding.viewpagerImages.adapter = null
        _binding = null
    }
}