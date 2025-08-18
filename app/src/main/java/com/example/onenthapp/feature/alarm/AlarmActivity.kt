package com.example.onenthapp.feature.alarm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.R
import com.example.onenthapp.data.alarm.DealAlarm
import com.example.onenthapp.data.alarm.PostAlarm
import com.example.onenthapp.data.alarm.AlarmRepository
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class AlarmActivity : AppCompatActivity() {

    private lateinit var adapter: AlarmAdapter
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var tabLayout: TabLayout

    private val repository = AlarmRepository()

    private val REQUEST_CODE_NOTIFICATION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        findViewById<ImageView>(R.id.btn_left).setOnClickListener {
            finish()
        }

        tabLayout = findViewById(R.id.tabLayout)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = AlarmAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 탭 추가
        tabLayout.addTab(tabLayout.newTab().setText("N분의 1"))
        tabLayout.addTab(tabLayout.newTab().setText("꿀팁 N분의 1"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> fetchDealAlarms()
                    1 -> fetchPostAlarms()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // 알림 권한 체크 및 요청 후 테스트 알림 보내기
        checkNotificationPermission()

        // 기본 탭 데이터 로드
        fetchDealAlarms()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 권한 요청
                ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE_NOTIFICATION)
            } else {
                // 권한 이미 있음 -> 테스트 알림 보내기
                sendTestPushNotification()
            }
        } else {
            // Android 13 미만 버전은 권한 필요 없음
            sendTestPushNotification()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 승인됨
                sendTestPushNotification()
            } else {
                // 권한 거절됨 - 알림 설정 화면으로 이동
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(intent)
                Toast.makeText(this, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun sendTestPushNotification() {
        lifecycleScope.launch {
            try {
                val response = repository.sendTestPush()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(this@AlarmActivity, "테스트 알림 발송 완료", Toast.LENGTH_SHORT).show()
                    Log.d("AlarmActivity", "테스트 푸시 성공: ${response.body()?.message}")
                } else {
                    Toast.makeText(this@AlarmActivity, "테스트 알림 발송 실패", Toast.LENGTH_SHORT).show()
                    Log.e("AlarmActivity", "실패 코드: ${response.code()}")
                }
            } catch (e: Exception) {
                Toast.makeText(this@AlarmActivity, "테스트 알림 에러: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("AlarmActivity", "예외 발생: ${e.message}")
            }
        }
    }

    private fun fetchDealAlarms() {
        lifecycleScope.launch {
            try {
                val response = repository.getDealAlarms()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val dealAlarms = response.body()?.result ?: emptyList()
                    adapter.updateData(mapDealAlarmsToItems(dealAlarms))
                } else {
                    Toast.makeText(this@AlarmActivity, "N분의 1 알림 조회 실패", Toast.LENGTH_SHORT).show()
                    Log.e("AlarmActivity", "N분의1 조회 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Toast.makeText(this@AlarmActivity, "에러: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("AlarmActivity", "N분의1 조회 에러: ${e.message}")
            }
        }
    }

    private fun fetchPostAlarms() {
        lifecycleScope.launch {
            try {
                val response = repository.getPostAlarms()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val postAlarms = response.body()?.result ?: emptyList()
                    adapter.updateData(mapPostAlarmsToItems(postAlarms))
                } else {
                    Toast.makeText(this@AlarmActivity, "꿀팁 알림 조회 실패", Toast.LENGTH_SHORT).show()
                    Log.e("AlarmActivity", "꿀팁 조회 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Toast.makeText(this@AlarmActivity, "에러: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("AlarmActivity", "꿀팁 조회 에러: ${e.message}")
            }
        }
    }

    // DealAlarm -> AlarmItem 변환 (탭 목록용)
    private fun mapDealAlarmsToItems(list: List<DealAlarm>): List<AlarmItem> {
        return list.map {
            AlarmItem(
                message = it.message,
                timeAgo = "방금 전",
                navigationImageResId = R.drawable.notification_ic_1,
                isRead = it.readStatus,
                type = getKoreanType(it.alertType) // <-- 여기!
            )
        }
    }



    // PostAlarm -> AlarmItem 변환
    private fun mapPostAlarmsToItems(list: List<PostAlarm>): List<AlarmItem> {
        return list.map {
            AlarmItem(
                message = it.message,
                timeAgo = "방금 전",
                navigationImageResId = R.drawable.notification_ic_2,
                isRead = it.readStatus,
                type = getKoreanType(it.alertType)
            )
        }
    }

    private fun getKoreanType(type: String): String {
        return when (type) {
            "REVIEW" -> "거래후기"
            "ITEM" -> "상품등록"
            "LIFE_TIP" -> "생활정보"
            "DISCOUNT" -> "할인정보"
            "RESTAURANT" -> "우리동네 맛집/카페"
            else -> "기타"
        }
    }


}
