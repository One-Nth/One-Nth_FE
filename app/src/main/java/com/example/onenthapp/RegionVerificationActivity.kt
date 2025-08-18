package com.example.onenthapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.onenthapp.data.map.MyRegion
import com.example.onenthapp.data.map.MyRegionRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.flexbox.FlexboxLayout
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import kotlinx.coroutines.launch

class RegionVerificationActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var kakaoMap: KakaoMap? = null
    private var markerLabelLayer: LabelLayer? = null
    private lateinit var flexSelected: FlexboxLayout
    private lateinit var btnAuthenticateRegion: Button
    private lateinit var tvDetail: TextView
    
    private val repository = MyRegionRepository()
    private var myRegions = listOf<MyRegion>()
    private var selectedRegion: MyRegion? = null
    private var currentLocation: Location? = null
    
    private companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_region_verification)

        initViews()
        setupMap()
        loadMyRegions()
        checkLocationPermission()
    }

    private fun initViews() {
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }
        
        mapView = findViewById(R.id.map)
        flexSelected = findViewById(R.id.flexSelected)
        btnAuthenticateRegion = findViewById(R.id.btnAuthenticateRegion)
        tvDetail = findViewById(R.id.detail)
        
        btnAuthenticateRegion.setOnClickListener { 
            authenticateSelectedRegion()
        }
    }

    private fun setupMap() {
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.d("RegionVerification", "onMapDestroy")
            }

            override fun onMapError(error: Exception) {
                Log.e("RegionVerification", "KakaoMap Error: ", error)
                Toast.makeText(this@RegionVerificationActivity, "지도 로딩 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                kakaoMap = map
                markerLabelLayer = kakaoMap?.labelManager?.layer
                
                // 기본 위치 설정 (서울 시청)
                val initialPosition = LatLng.from(37.5665, 126.9780)
                kakaoMap?.moveCamera(
                    CameraUpdateFactory.newCenterPosition(initialPosition, 15)
                )
                
                // 현재 위치가 있으면 표시
                currentLocation?.let { showCurrentLocationOnMap(it) }
            }

            override fun getZoomLevel(): Int = 15
        })
    }

    private fun loadMyRegions() {
        lifecycleScope.launch {
            try {
                myRegions = repository.getMyRegions()
                renderRegionChips()
                
                // 기본적으로 첫 번째 지역(메인 지역) 선택
                if (myRegions.isNotEmpty()) {
                    selectRegion(myRegions.first())
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegionVerificationActivity, "지역 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("RegionVerification", "Failed to load regions", e)
            }
        }
    }

    private fun renderRegionChips() {
        flexSelected.removeAllViews()
        
        myRegions.forEachIndexed { index, region ->
            val chip = Chip(this, null, com.google.android.material.R.attr.chipStyle).apply {
                setChipDrawable(
                    ChipDrawable.createFromAttributes(
                        this@RegionVerificationActivity, null, 0, R.style.Widget_OneNth_RegionChip
                    )
                )
                text = extractDong(region.regionName)
                isCheckable = true
                isChecked = (index == 0) // 첫 번째 칩이 기본 선택
                
                val textColorStateList = ContextCompat.getColorStateList(this@RegionVerificationActivity, R.color.region_chip_text)
                setTextColor(textColorStateList)
                
                setOnClickListener {
                    selectRegion(region)
                    updateChipSelection(this)
                }
            }
            flexSelected.addView(chip)
        }
    }

    private fun selectRegion(region: MyRegion) {
        selectedRegion = region
        tvDetail.text = "현재 위치가 내 지역으로 등록된 ${extractDong(region.regionName)}에 있는지 확인해보세요."
        
        // 지도에 지역 마커 표시
        lifecycleScope.launch {
            try {
                val regionCenter = repository.getRegionCenter(region.regionName)
                regionCenter?.let {
                    showRegionOnMap(LatLng.from(it.latitude, it.longitude), extractDong(region.regionName))
                }
            } catch (e: Exception) {
                Log.e("RegionVerification", "Failed to get region center", e)
            }
        }
    }

    private fun updateChipSelection(selectedChip: Chip) {
        for (i in 0 until flexSelected.childCount) {
            val chip = flexSelected.getChildAt(i) as? Chip
            chip?.isChecked = (chip == selectedChip)
        }
    }

    private fun extractDong(regionName: String): String {
        return regionName.split(" ").lastOrNull()?.replace("동", "동") ?: "OO동"
    }

    private fun showRegionOnMap(position: LatLng, regionName: String) {
        kakaoMap?.let { map ->
            markerLabelLayer?.removeAll()
            
            // 지역 마커 추가
            val labelStyles = LabelStyles.from(LabelStyle.from(R.drawable.ic_map_marker_green))
            val textBuilder = LabelTextBuilder().addTextLine(regionName, 0)
            val options = LabelOptions.from(position).setStyles(labelStyles).setTexts(textBuilder)
            markerLabelLayer?.addLabel(options)
            
            // 카메라 이동
            map.moveCamera(CameraUpdateFactory.newCenterPosition(position, 15))
        }
    }

    private fun showCurrentLocationOnMap(location: Location) {
        kakaoMap?.let { map ->
            val position = LatLng.from(location.latitude, location.longitude)
            
            // 현재 위치 마커 추가 (파란색)
            val labelStyles = LabelStyles.from(LabelStyle.from(R.drawable.ic_map_marker_gray))
            val textBuilder = LabelTextBuilder().addTextLine("현재 위치", 0)
            val options = LabelOptions.from(position).setStyles(labelStyles).setTexts(textBuilder)
            markerLabelLayer?.addLabel(options)
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            return
        }
        
        try {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) 
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            
            location?.let {
                currentLocation = it
                showCurrentLocationOnMap(it)
            }
        } catch (e: Exception) {
            Log.e("RegionVerification", "Failed to get location", e)
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
            showLocationRequiredDialog()
            return
        }
        
        lifecycleScope.launch {
            try {
                btnAuthenticateRegion.isEnabled = false
                btnAuthenticateRegion.text = "인증 중..."
                
                val result = repository.authenticateRegion(
                    region.regionId, 
                    location.latitude, 
                    location.longitude
                )
                
                result?.let {
                    if (it.verified) {
                        showSuccessDialog(it.detectedRegionName, it.requestedRegionName)
                    } else {
                        showFailureDialog(it.detectedRegionName, it.requestedRegionName)
                    }
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@RegionVerificationActivity, "지역 인증에 실패했습니다: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("RegionVerification", "Authentication failed", e)
            } finally {
                btnAuthenticateRegion.isEnabled = true
                btnAuthenticateRegion.text = "지역 인증하기"
            }
        }
    }

    private fun showLocationRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("위치 정보 필요")
            .setMessage("지역 인증을 위해 현재 위치 정보가 필요합니다. 위치 서비스를 활성화해주세요.")
            .setPositiveButton("다시 시도") { _, _ -> 
                getCurrentLocation()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showSuccessDialog(detectedRegion: String, requestedRegion: String) {
        AlertDialog.Builder(this)
            .setTitle("인증 성공!")
            .setMessage("현재 위치($detectedRegion)가 등록된 지역($requestedRegion)과 일치합니다.")
            .setPositiveButton("확인") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showFailureDialog(detectedRegion: String, requestedRegion: String) {
        AlertDialog.Builder(this)
            .setTitle("인증 실패")
            .setMessage("현재 위치($detectedRegion)가 등록된 지역($requestedRegion)과 일치하지 않습니다.")
            .setPositiveButton("확인", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        mapView.pause()
    }
}
