package com.example.onenthapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.onenthapp.databinding.FragmentTipsCafetipsBinding
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
// import com.kakao.vectormap.MapView // MapView 클래스 자체는 바인딩 객체를 통해 접근하므로 직접 임포트는 선택적

class CafeTipsFragment : Fragment() {

    private var _binding: FragmentTipsCafetipsBinding? = null
    private val binding get() = _binding!! // non-null assertion operator

    private var kakaoMapInstance: KakaoMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View { // 반환 타입을 View? 에서 View로 변경 (binding 사용 시)
        _binding = FragmentTipsCafetipsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MapView 초기화
        binding.mapViewDisplay.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.d("DiscountTipsFragment", "onMapDestroy")
                // 지도 API 가 정상적으로 종료될 때 호출됨
            }

            override fun onMapError(error: Exception) {
                Log.e("DiscountTipsFragment", "onMapError: ${error.message}", error)
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
                // 사용자에게 오류 메시지를 보여주는 등의 처리를 할 수 있습니다.
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                Log.d("DiscountTipsFragment", "KakaoMap is ready for DiscountTips.")
                kakaoMapInstance = kakaoMap

                // --- 여기에 지도 관련 로직 추가 ---
                // 예: 초기 카메라 위치, 마커 표시, 이벤트 리스너 등
                // val initialPosition = LatLng.from(37.537229, 127.005515) // 예시: 이태원역
                // kakaoMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 16))
                //
                // 할인 정보와 관련된 마커들을 표시하는 로직을 여기에 구현합니다.
                // loadDiscountMarkers(kakaoMap)
            }
        })
    }

    // 할인 정보 마커를 로드하고 지도에 표시하는 함수의 예시
    private fun loadDiscountMarkers(kakaoMap: KakaoMap) {
        // ViewModel이나 Repository에서 할인 정보를 가져와서 마커로 표시
        // 예시:
        // val discountItems = viewModel.getDiscountLocations()
        // discountItems.forEach { item ->
        //     val markerOptions = LabelOptions.from(LatLng.from(item.latitude, item.longitude))
        //         .setStyles(yourLabelStyle) // 미리 정의된 라벨 스타일
        //         .setTexts(item.name)
        //     kakaoMap.labelManager?.layer?.addLabel(markerOptions)
        // }
    }


    override fun onResume() {
        super.onResume()
        // binding이 null이 아닐 때만 MapView의 resume을 호출하도록 방어 코드 추가
        // 프래그먼트의 뷰가 아직 생성되지 않았거나 파괴된 후 onResume이 호출될 가능성을 배제
        if (_binding != null) {
            binding.mapViewDisplay.resume()
            Log.d("DiscountTipsFragment", "MapView resumed")
        } else {
            Log.d("DiscountTipsFragment", "onResume called but binding is null, MapView not resumed.")
        }
    }

    override fun onPause() {
        super.onPause()
        if (_binding != null) {
            binding.mapViewDisplay.pause()
            Log.d("DiscountTipsFragment", "MapView paused")
        } else {
            Log.d("DiscountTipsFragment", "onPause called but binding is null, MapView not paused.")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("DiscountTipsFragment", "onDestroyView called")
        // MapView의 리소스를 정리.
        // binding.mapViewDisplay.finish() // finish() 호출 시점은 앱의 로직에 따라 조절
        // 탭 전환 시 프래그먼트가 destroyView 후 재생성된다면 finish() 호출 필요.
        // 만약 hide/show 방식으로 프래그먼트를 관리한다면 호출하지 않을 수 있음.
        // ViewPager2의 기본 동작은 destroyView 후 재생성이므로 호출하는 것이 안전.
        if (_binding != null && binding.mapViewDisplay.isAttachedToWindow) { // finish는 MapView가 attach되어 있을 때만 호출
            // binding.mapViewDisplay.finish()
            // finish()는 주의해서 사용해야 합니다.
            // 만약 finish() 후 다시 start()를 하려면 MapView를 새로 inflate해야 할 수 있습니다.
            // 일반적으로 ViewPager2 환경에서는 pause/resume만으로도 충분할 수 있으며,
            // 진정한 '종료'는 Fragment가 완전히 제거될 때 고려합니다.
            // 카카오맵 SDK 문서에서 권장하는 생명주기 관리 방식을 따르는 것이 가장 좋습니다.
        }
        _binding = null // 메모리 누수 방지를 위해 명시적으로 null 할당
        kakaoMapInstance = null
    }
}
