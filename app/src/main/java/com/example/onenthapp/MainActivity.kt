package com.example.onenthapp

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.onenthapp.feature.chat.ChatActivity
import com.example.onenthapp.databinding.ActivityMainBinding
import com.example.onenthapp.model.HomeTabType
import com.example.onenthapp.model.SharedViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val sharedViewModel: SharedViewModel by viewModels()

    // Tip 탭의 현재 게시판 타입은 FAB 클릭 시점에 직접 확인

    private enum class FabMode { TIP_POST_WRITE, PRODUCT_REGISTER }
    private var fabMode: FabMode = FabMode.PRODUCT_REGISTER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        // BottomNavigationView
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // 플러스는 FAB가 담당
                R.id.plusFragment -> false

                // 채팅 아이콘은 Activity로
                R.id.chatFragment -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    false
                }

                // 통계 화면 Activity
                R.id.statsFragment -> {
                    startActivity(Intent(this, NwonSavedActivity::class.java))
                    false
                }

                // 그 외는 NavController가 처리
                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                    true
                }
            }
        }


        // 검색 결과에서 상세로 이동 요청 처리 (인텐트 플래그)
        intent?.let { maybeIntent ->
            if (maybeIntent.getBooleanExtra("navigate_to_detail", false)) {
                val productId = maybeIntent.getLongExtra("detail_item_id", -1L)
                val isShare = maybeIntent.getBooleanExtra("detail_is_share", false)
                val initialScraped = maybeIntent.getBooleanExtra("detail_initial_scraped", false)
                if (productId != -1L) {
                    if (isShare) {
                        navController.navigate(
                            R.id.action_home_to_sharedetail,
                            Bundle().apply {
                                putLong("productId", productId)
                                putBoolean("initialScraped", initialScraped)
                            }
                        )
                    } else {
                        navController.navigate(
                            R.id.action_home_to_buydetail,
                            Bundle().apply {
                                putLong("productId", productId)
                                putBoolean("initialScraped", initialScraped)
                            }
                        )
                    }
                }
            }
        }

        // 화면 이동 시 하단바/FAB 노출 & FAB 모드 전환
        navController.addOnDestinationChangedListener { _, dest, _ ->
            val hideOn = setOf(
                R.id.action_global_complete,
                R.id.groupPurchaseDetailFragment,
                R.id.action_home_to_buydetail,
                R.id.action_home_to_sharedetail,
                R.id.productDetailFragment,
                R.id.plusBuyFragment,
                R.id.plusShareFragment,
                R.id.statsFragment,
                R.id.chatFragment
            )
            val shouldHide = dest.id in hideOn

            binding.bottomNavigationView.isVisible = !shouldHide
            binding.fabAdd.isVisible = !shouldHide

            // Tip 화면이면 글쓰기 모드, 아니면 상품 등록 모드
            if (dest.id == R.id.tipFragment) setFabAsTip() else setFabAsProduct()
        }

        // ✅ FAB 클릭 (한 번만)
        binding.fabAdd.setOnClickListener {
            val onTipScreen = navController.currentDestination?.id == R.id.tipFragment
            if (onTipScreen && fabMode == FabMode.TIP_POST_WRITE) {
                // TipFragment에서 현재 탭 확인하는 메서드 호출
                val tipFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    ?.childFragmentManager?.fragments?.firstOrNull { it is TipFragment } as? TipFragment
                
                val currentPosition = tipFragment?.getCurrentTabPosition() ?: 1 // 기본값: 생활꿀팁
                val realPostType = when (currentPosition) {
                    0 -> "DISCOUNT"
                    1 -> "LIFE_TIP"
                    else -> "RESTAURANT"
                }
                
                startActivity(
                    Intent(this, CreateLifePostActivity::class.java)
                        .putExtra("postType", realPostType)
                )
            } else {
                // 상품 등록 (현재 홈 탭에 따라)
                val dest = when (sharedViewModel.currentHomeTab.value) {
                    HomeTabType.BUY -> R.id.plusBuyFragment
                    else -> R.id.plusShareFragment
                }
                navController.navigate(dest)
            }
        }

        // 권한 체크
        val p1 = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
        val p2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val p3 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (p1 == PackageManager.PERMISSION_DENIED ||
            p2 == PackageManager.PERMISSION_DENIED ||
            p3 == PackageManager.PERMISSION_DENIED
        ) {
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

    private fun setFabAsProduct() {
        fabMode = FabMode.PRODUCT_REGISTER
        binding.fabAdd.setImageResource(R.drawable.ic_navigation_plus)
        binding.fabAdd.contentDescription = "상품 등록"
    }

    private fun setFabAsTip() {
        fabMode = FabMode.TIP_POST_WRITE
        binding.fabAdd.setImageResource(R.drawable.ic_navigation_plus)
        binding.fabAdd.contentDescription = "게시글 쓰기"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grandResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grandResults)
        if (requestCode == 1000) {
            val allGranted = grandResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (!allGranted) finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        intent?.let { maybeIntent ->
            if (maybeIntent.getBooleanExtra("navigate_to_detail", false)) {
                val productId = maybeIntent.getLongExtra("detail_item_id", -1L)
                val isShare = maybeIntent.getBooleanExtra("detail_is_share", false)
                val initialScraped = maybeIntent.getBooleanExtra("detail_initial_scraped", false)
                if (productId != -1L) {
                    if (isShare) {
                        navController.navigate(
                            R.id.action_home_to_sharedetail,
                            Bundle().apply {
                                putLong("productId", productId)
                                putBoolean("initialScraped", initialScraped)
                            }
                        )
                    } else {
                        navController.navigate(
                            R.id.action_home_to_buydetail,
                            Bundle().apply {
                                putLong("productId", productId)
                                putBoolean("initialScraped", initialScraped)
                            }
                        )
                    }
                }
                maybeIntent.removeExtra("navigate_to_detail")
            }
        }
    }
}

