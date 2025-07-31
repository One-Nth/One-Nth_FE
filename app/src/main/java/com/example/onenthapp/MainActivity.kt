package com.example.onenthapp

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.onenthapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.plusFragment -> {
                    false
                }

                R.id.chatFragment -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    false
                }

                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                    true
                }
            }
        }

        // FAB 클릭시 상품 등록 화면으로 이동
        binding.fabAdd.setOnClickListener {
            val dest = when (sharedViewModel.currentHomeTab.value) {
                HomeTabType.BUY -> R.id.plusBuyFragment
                else               -> R.id.plusShareFragment
            }
            navController.navigate(dest)
        }
        navController.addOnDestinationChangedListener { _, dest, _ ->
            val hideOn = setOf(
                R.id.action_search_to_productdetail,
                R.id.action_home_to_productdetail,
                R.id.productDetailFragment,
                R.id.plusBuyFragment,
                R.id.plusShareFragment,
                R.id.statsFragment,
                R.id.chatFragment,
            ).contains(dest.id)

            binding.bottomNavigationView.isVisible = !hideOn
            binding.fabAdd.isVisible = !hideOn
        }

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.INTERNET
        )

        val permission2 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val permission3 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // 권한이 열려있는지 확인
        if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED || permission3 == PackageManager.PERMISSION_DENIED) {
            // 권한 체크(READ_PHONE_STATE의 requestCode를 1000으로 세팅
            requestPermissions(
                arrayOf(
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1000
            )
            return
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grandResults: IntArray
    ) {
        // READ_PHONE_STATE의 권한 체크 결과
        super.onRequestPermissionsResult(requestCode, permissions, grandResults)
        if (requestCode == 1000) {
            var check_result = true
            // 모든 퍼미션을 허용했는지 체크
            for (result in grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false
                    break
                }
            }
            // 권한 체크에 동의를 하지 않으면 종료
            if (!check_result) {
                finish()
            }
        }
    }
}
