package com.example.onenthapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import com.google.android.material.appbar.MaterialToolbar
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuPopupHelper

import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback

class HomeFragment : Fragment(), OnMapReadyCallback {
    private var naverMap: NaverMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        return view    }

    @SuppressLint("RestrictedApi", "DiscouragedPrivateApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fm = childFragmentManager
        var mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }

        mapFragment.getMapAsync(this) // this는 OnMapReadyCallback을 구현한 HomeFragment 자신

//// 1) Toolbar menu inflate
//        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar_home)
//        toolbar.inflateMenu(R.menu.menu_home)
//        toolbar.setOnMenuItemClickListener { item ->
//            if (item.itemId == R.id.action_notifications) {
//                // TODO: 알림 화면으로 이동
//            }
//            true
//        }

        // 2) 드롭다운 PopupMenu
        val arrow = view.findViewById<ImageView>(R.id.iv_arrow)
        val titleContainer = view.findViewById<LinearLayout>(R.id.ll_title_container)
        val popup = PopupMenu(
            ContextThemeWrapper(requireContext(), R.style.Theme_OneNthApp),
            arrow,
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
                        true
                    }
                    else -> false
                }
            }
            setOnDismissListener {
                // 팝업 닫힐 때 화살표 복귀
                arrow.animate().rotation(0f).start()
            }
        }

        // 4) 클릭하면 화살표 회전 + 팝업 보여주기
        arrow.setOnClickListener {
            arrow.animate().rotation(180f).start()
            popup.show()
        }
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        // 네이버 지도 객체를 받고 필요한 설정
        this.naverMap = naverMap
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true)
        // 다른 지도 관련 로직 추가 (예: 마커 추가, 카메라 이동 등)
    }

}
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fm = childFragmentManager
        var mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true)
    }
}