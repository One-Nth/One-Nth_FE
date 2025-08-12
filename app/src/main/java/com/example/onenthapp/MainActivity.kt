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
import com.example.onenthapp.chat.ChatActivity
import com.example.onenthapp.databinding.ActivityMainBinding
import com.example.onenthapp.model.HomeTabType
import com.example.onenthapp.model.SharedViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var tipPostType: String = "LIFE_TIP" // 기본값

    private val sharedViewModel: SharedViewModel by viewModels()

    /** 생활꿀팁 글쓰기 vs 상품 등록 */
    private enum class FabMode { TIP_POST_WRITE, PRODUCT_REGISTER }
    private var fabMode: FabMode = FabMode.PRODUCT_REGISTER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = (supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        // BottomNavigationView
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.plusFragment -> false
                R.id.chatFragment -> { startActivity(Intent(this, ChatActivity::class.java)); false }
                else -> { NavigationUI.onNavDestinationSelected(item, navController); true }
            }
        }

        // ✅ TipFragment에서 현재 탭의 postType 수신
        supportFragmentManager.setFragmentResultListener("board_tab", this) { _, b ->
            tipPostType = b.getString("postType") ?: "LIFE_TIP"
            setFabAsTip() // Tip 화면일 땐 항상 글쓰기 모드
        }

        // 화면 이동 시 FAB 표시/모드
        navController.addOnDestinationChangedListener { _, dest, _ ->
            val hideOn = setOf(
                R.id.action_search_to_productdetail, R.id.action_global_complete,
                R.id.groupPurchaseDetailFragment, R.id.action_home_to_buydetail,
                R.id.productDetailFragment, R.id.plusBuyFragment, R.id.plusShareFragment,
                R.id.statsFragment, R.id.chatFragment
            ).contains(dest.id)

            binding.bottomNavigationView.isVisible = !hideOn
            binding.fabAdd.isVisible = !hideOn

            // Tip이 아닐 땐 상품 등록 모드로
            if (dest.id != R.id.tipFragment) setFabAsProduct()
            else setFabAsTip()
        }

        // ✅ FAB 클릭
        binding.fabAdd.setOnClickListener {
            val destId = navController.currentDestination?.id
            if (destId == R.id.tipFragment && fabMode == FabMode.TIP_POST_WRITE) {
                // 탭에 맞는 postType을 들고 "하나의 글쓰기 화면"으로 이동
                startActivity(Intent(this, CreateLifePostActivity::class.java).apply {
                    putExtra("postType", tipPostType) // "DISCOUNT" | "LIFE_TIP" | "RESTAURANT"
                })
            } else {
                // 기존 상품 등록
                val dest = when (sharedViewModel.currentHomeTab.value) {
                    HomeTabType.BUY -> R.id.plusBuyFragment
                    else -> R.id.plusShareFragment
                }
                navController.navigate(dest)
            }
        }

        // 화면 바뀔 때 FAB 노출/모드 보정
        navController.addOnDestinationChangedListener { _, dest, _ ->
            val hideOn = setOf(
                R.id.action_search_to_productdetail,
                R.id.action_global_complete,
                R.id.groupPurchaseDetailFragment,
                R.id.action_home_to_buydetail,
                R.id.productDetailFragment,
                R.id.plusBuyFragment,
                R.id.plusShareFragment,
                R.id.statsFragment,
                R.id.chatFragment,
            ).contains(dest.id)

            binding.bottomNavigationView.isVisible = !hideOn
            binding.fabAdd.isVisible = !hideOn

            // Tip 화면이 아니면 기본(상품 등록) 모드로 복귀
            if (dest.id != R.id.tipFragment) setFabAsProduct()
        }

        // FAB 클릭
        binding.fabAdd.setOnClickListener {
            if (fabMode == FabMode.TIP_POST_WRITE &&
                navController.currentDestination?.id == R.id.tipFragment
            ) {
                // 생활꿀팁 글쓰기
                startActivity(Intent(this, CreateLifePostActivity::class.java))
            } else {
                // 상품 등록
                val dest = when (sharedViewModel.currentHomeTab.value) {
                    HomeTabType.BUY -> R.id.plusBuyFragment
                    else            -> R.id.plusShareFragment
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

    private fun setFabAsTip() {           // ★ 게시글 쓰기 모드
        fabMode = FabMode.TIP_POST_WRITE      // ★ 여기서 모드 바꿈
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
}

