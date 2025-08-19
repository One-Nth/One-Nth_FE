package com.example.onenthapp.feature.mypage

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.onenthapp.R
import com.example.onenthapp.data.map.MyRegion
import com.example.onenthapp.data.map.MyRegionRepository
import com.example.onenthapp.databinding.ActivityRegionVerificationBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import kotlinx.coroutines.launch

class RegionVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegionVerificationBinding

    // Kakao Map
    private var kakaoMap: KakaoMap? = null
    private var markerLabelLayer: LabelLayer? = null

    // Data
    private val repository = MyRegionRepository()
    private var myRegions: List<MyRegion> = emptyList()
    private var selectedRegion: MyRegion? = null
    private var currentLocation: Location? = null

    private companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegionVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()
        setupMap()
        loadMyRegions()
        checkLocationPermission()
    }


    private fun initUi() = with(binding) {
        backButton.setOnClickListener { finish() }
        btnAuthenticateRegion.setOnClickListener { authenticateSelectedRegion() }
        // 초기엔 선택 상태 그룹만 보이도록(레이아웃 기본값과 동일)
        groupSelect.visibility = View.VISIBLE
        groupResult.visibility = View.GONE
        tvSuccess.visibility = View.GONE
        tvFail.visibility = View.GONE
        tvFailNote.visibility = View.GONE
        btnVerifyDone.setOnClickListener { finish() }
        btnChangeNotAllowed.setOnClickListener {
            groupResult.visibility = View.GONE
            groupSelect.visibility = View.VISIBLE

            // 결과 텍스트/상태 초기화
            tvSuccess.visibility = View.GONE
            tvFail.visibility = View.GONE
            tvFailNote.visibility = View.GONE
 }
    }


    private fun setupMap() = with(binding) {
        map.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {}
            override fun onMapError(error: Exception) {
                Log.e("RegionVerification", "KakaoMap Error", error)
                Toast.makeText(this@RegionVerificationActivity, "지도 로딩 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                kakaoMap = map
                markerLabelLayer = kakaoMap?.labelManager?.layer
                // 기본 카메라
                kakaoMap?.moveCamera(
                    CameraUpdateFactory.newCenterPosition(LatLng.from(37.5665, 126.9780), 15)
                )
                currentLocation?.let { showCurrentLocationOnMap(it) }
            }
            override fun getZoomLevel(): Int = 15
        })
    }

    private fun showRegionOnMap(pos: LatLng, dong: String) {
        val map = kakaoMap ?: return
        // 센터 마커 1개만 유지
        markerLabelLayer?.removeAll()

        val style = LabelStyle.from(R.drawable.marker_green_72)
            .setAnchorPoint(0.5f, 1.0f)
            .setTextStyles(
                25,            // 폰트 크기
                Color.BLACK,   // 글자색
                1,             // 외곽선 두께
                Color.WHITE    // 외곽선 색
            )
        val options = LabelOptions.from(pos)
            .setStyles(LabelStyles.from(style))
            .setTexts(LabelTextBuilder().addTextLine(dong, 0))
            .setRank(0)

        markerLabelLayer?.addLabel(options)
        map.moveCamera(CameraUpdateFactory.newCenterPosition(pos, 15))
    }

    private fun showCurrentLocationOnMap(location: Location) {
        kakaoMap ?: return
        val pos = LatLng.from(location.latitude, location.longitude)
        val gray = LabelStyles.from(LabelStyle.from(R.drawable.ic_map_marker_gray))
        val opt = LabelOptions.from(pos).setStyles(gray)
            .setTexts(LabelTextBuilder().addTextLine("현재 위치", 0))
        markerLabelLayer?.addLabel(opt)
    }


    private fun loadMyRegions() {
        lifecycleScope.launch {
            try {
                myRegions = repository.getMyRegions()
                renderRegionChips()
            } catch (e: Exception) {
                Log.e("RegionVerification", "loadMyRegions", e)
                Toast.makeText(this@RegionVerificationActivity, "지역 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderRegionChips() = with(binding) {
        flexSelected.removeAllViews()
        myRegions.forEachIndexed { index, region ->
            val chip = Chip(this@RegionVerificationActivity, null, com.google.android.material.R.attr.chipStyle).apply {
                setChipDrawable(
                    ChipDrawable.createFromAttributes(
                        this@RegionVerificationActivity, null, 0, R.style.Widget_OneNth_RegionChip
                    )
                )
                text = extractDong(region.regionName)
                isCheckable = true
                isChecked = (index == 0)
                setTextColor(ContextCompat.getColorStateList(context, R.color.region_chip_text))

                setOnClickListener {
                    selectRegion(region)
                    updateChipSelection(this)
                }
            }
            val lp = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            val margin = (35 * resources.displayMetrics.density).toInt()
            lp.setMargins(0, 0, margin, 0)
            chip.layoutParams = lp
            flexSelected.addView(chip)

            if (index == 0) selectRegion(region) // 첫 칩 선택 시 바로 지도에 표시
        }
    }

    private fun updateChipSelection(selectedChip: Chip) = with(binding) {
        for (i in 0 until flexSelected.childCount) {
            (flexSelected.getChildAt(i) as? Chip)?.isChecked =
                (flexSelected.getChildAt(i) === selectedChip)
        }
    }

    private fun selectRegion(region: MyRegion) {
        selectedRegion = region
        // 선택 시 OO동 중심으로 지도/마커 선표시
        lifecycleScope.launch {
            try {
                repository.getRegionCenter(region.regionName)?.let { c ->
                    showRegionOnMap(LatLng.from(c.latitude, c.longitude), extractDong(region.regionName))
                }
            } catch (e: Exception) {
                Log.e("RegionVerification", "getRegionCenter", e)
            }
        }
    }

    private fun extractDong(regionName: String): String =
        regionName.split(" ").lastOrNull() ?: "OO동"


    private enum class VerifyResult { SUCCESS, FAIL }

    private fun showVerifyResult(state: VerifyResult, dong: String) = with(binding) {
        // 선택 상태 그룹 숨김, 결과 그룹 표시
        groupSelect.visibility = View.GONE
        groupResult.visibility = View.VISIBLE

        when (state) {
            VerifyResult.SUCCESS -> {
                tvSuccess.visibility = View.VISIBLE
                tvSuccess.text = "현재 위치가 내 동네로 설정된 ${dong}에 있습니다."
                tvFail.visibility = View.GONE
                tvFailNote.visibility = View.GONE
                btnVerifyDone.visibility = View.VISIBLE
                btnChangeNotAllowed.visibility = View.GONE
            }
            VerifyResult.FAIL -> {
                tvSuccess.visibility = View.GONE
                tvFail.visibility = View.VISIBLE
                tvFail.text = "현재 위치가 내 동네로 설정된 ${dong}에 있지 않습니다."
                tvFailNote.visibility = View.VISIBLE
                btnVerifyDone.visibility = View.GONE
                btnChangeNotAllowed.visibility = View.VISIBLE
            }
        }
    }

    private fun authenticateSelectedRegion() {
        val region = selectedRegion
        val location = currentLocation

        if (region == null) {
            Toast.makeText(this, "인증할 지역을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (location == null) {
            showLocationRequiredDialog(); return
        }

        lifecycleScope.launch {
            try {
                // 링크 느낌 유지: 텍스트만 잠시 변경
                binding.btnAuthenticateRegion.isEnabled = false
                binding.btnAuthenticateRegion.text = "인증 중..."

                val result = repository.authenticateRegion(
                    region.regionId, location.latitude, location.longitude
                )

                val dong = extractDong(region.regionName)

                // 지도는 항상 선택한 동의 중심으로 업데이트
                repository.getRegionCenter(region.regionName)?.let { c ->
                    showRegionOnMap(LatLng.from(c.latitude, c.longitude), dong)
                }

                if (result?.verified == true) {
                    showVerifyResult(VerifyResult.SUCCESS, dong)
                } else {
                    showVerifyResult(VerifyResult.FAIL, dong)
                }

            } catch (e: Exception) {
                Log.e("RegionVerification", "authenticate", e)
                Toast.makeText(this@RegionVerificationActivity, "지역 인증에 실패했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnAuthenticateRegion.isEnabled = true
                binding.btnAuthenticateRegion.text = "동네 인증하기"
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        try {
            val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            loc?.let {
                currentLocation = it
                showCurrentLocationOnMap(it)
            }
        } catch (e: Exception) {
            Log.e("RegionVerification", "getCurrentLocation", e)
        }
    }

    private fun showLocationRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("위치 정보 필요")
            .setMessage("지역 인증을 위해 현재 위치 정보가 필요합니다. 위치 서비스를 활성화해주세요.")
            .setPositiveButton("다시 시도") { _, _ -> getCurrentLocation() }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() { super.onResume(); binding.map.resume() }
    override fun onPause() { super.onPause(); binding.map.pause() }
}
