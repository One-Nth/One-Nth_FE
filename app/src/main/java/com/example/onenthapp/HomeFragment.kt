package com.example.onenthapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.UiThread
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import com.example.onenthapp.databinding.FragmentHomeBinding
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var naverMap: NaverMap? = null

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

    @SuppressLint("RestrictedApi", "DiscouragedPrivateApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 지도 Fragment 설정
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)

        // 2. 드롭다운 메뉴 설정
        val popup = PopupMenu(
            ContextThemeWrapper(requireContext(), R.style.Theme_OneNthApp),
            binding.ivArrow,
            Gravity.END
        ).apply {
            menuInflater.inflate(R.menu.menu_title_dropdown, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_n_1 -> {
                        // "N분의1" 선택 처리
                        true
                    }
                    R.id.menu_tip_n_1 -> {
                        // "꿀팁 N분의1" 선택 처리
                        true
                    }
                    else -> false
                }
            }
            setOnDismissListener {
                binding.ivArrow.animate().rotation(0f).start()
            }
        }

        // 3. 화살표 클릭 시 팝업 열기
        binding.ivArrow.setOnClickListener {
            binding.ivArrow.animate().rotation(180f).start()
            popup.show()
        }

        // 4. 알림 버튼 클릭 시 AlarmActivity로 이동
        binding.btNotification.setOnClickListener {
            val intent = Intent(requireContext(), AlarmActivity::class.java)
            startActivity(intent)
        }
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true)
        // 지도 설정 추가 가능
    }
}
