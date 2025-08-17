package com.example.onenthapp.feature.alarm

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.R
import com.google.android.material.tabs.TabLayout

class AlarmActivity : AppCompatActivity() {

    private lateinit var adapter: AlarmAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        findViewById<ImageView>(R.id.btn_left).setOnClickListener {
            finish()
        }


        tabLayout = findViewById(R.id.tabLayout)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = AlarmAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 탭 추가
        tabLayout.addTab(tabLayout.newTab().setText("N분의 1"))
        tabLayout.addTab(tabLayout.newTab().setText("꿀팁 N분의 1"))

        // 탭 클릭 리스너
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> adapter.updateData(getBuyTogetherData())
                    1 -> adapter.updateData(getShareTogetherData())
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // 기본 첫 탭 데이터
        adapter.updateData(getBuyTogetherData())
    }

    // 예시 데이터
    private fun getBuyTogetherData(): List<AlarmItem> {
        return listOf(
            AlarmItem("같이사요 - 알림 1", "방금 전", R.drawable.notification_ic_1),
            AlarmItem("함께나눠요 - 알림 2", "1분 전", R.drawable.notification_ic_1),
            AlarmItem("같이사요 - 알림 3", "3분 전", R.drawable.notification_ic_1)
        )
    }

    private fun getShareTogetherData(): List<AlarmItem> {
        return listOf(
            AlarmItem("생활 정보 - 알림 1", "2초 전", R.drawable.notification_ic_2),
            AlarmItem("우리동네 맛집/카페 - 알림 2", "5분 전", R.drawable.notification_ic_2)
        )
    }

}
