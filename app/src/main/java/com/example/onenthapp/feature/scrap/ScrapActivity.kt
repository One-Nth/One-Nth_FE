package com.example.onenthapp.feature.scrap

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.example.onenthapp.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ScrapActivity : AppCompatActivity() {

    companion object {
        const val SEARCH_KEY = "GLOBAL_SEARCH_QUERY"
        const val SEARCH_BUNDLE_KEY = "q"
    }

    private lateinit var navController: NavController
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrap)

        tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        viewPager = findViewById<ViewPager2>(R.id.viewPager)

        // NavController 초기화
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.scrap_nav_host) as NavHostFragment
        navController = navHostFragment.navController

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        val adapter = ScrapPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "N분의 1"
                1 -> "꿀팁 N분의 1"
                else -> ""
            }
        }.attach()

        // ✅ 공용 검색창(한 줄 + 검색 액션)
        val searchEt = findViewById<EditText>(R.id.search_bar_et).apply {
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            setSingleLine(true)
            maxLines = 1
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }

        // 타이핑할 때마다 브로드캐스트
        searchEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                supportFragmentManager.setFragmentResult(
                    SEARCH_KEY, bundleOf(SEARCH_BUNDLE_KEY to (s?.toString().orEmpty()))
                )
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 엔터(검색) 눌러도 동일 처리 + 줄바꿈 방지
        searchEt.setOnEditorActionListener { v, actionId, event ->
            val isEnter = event?.keyCode == android.view.KeyEvent.KEYCODE_ENTER &&
                    event.action == android.view.KeyEvent.ACTION_UP
            if (actionId == EditorInfo.IME_ACTION_SEARCH || isEnter) {
                supportFragmentManager.setFragmentResult(
                    SEARCH_KEY, bundleOf(SEARCH_BUNDLE_KEY to (searchEt.text?.toString().orEmpty()))
                )
                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                        as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(searchEt.windowToken, 0)
                searchEt.clearFocus()
                true
            } else false
        }

        // 초기 검색어도 한 번 쏘기
        supportFragmentManager.setFragmentResult(
            SEARCH_KEY, bundleOf(SEARCH_BUNDLE_KEY to (searchEt.text?.toString().orEmpty()))
        )
    }

    /** 상품 상세 화면 표시 */
    fun showProductDetail(productId: Long, isShare: Boolean, initialScraped: Boolean) {
        // 기존 UI 숨기기
        findViewById<View>(R.id.top_bar).visibility = View.GONE
        findViewById<View>(R.id.search_bar).visibility = View.GONE
        tabLayout.visibility = View.GONE
        viewPager.visibility = View.GONE
        
        // NavHost 표시
        findViewById<View>(R.id.scrap_nav_host).visibility = View.VISIBLE
        
        // 상품 상세로 이동
        if (isShare) {
            navController.navigate(
                R.id.sharingItemDetailFragment,
                bundleOf(
                    "productId" to productId,
                    "initialScraped" to initialScraped
                )
            )
        } else {
            navController.navigate(
                R.id.groupPurchaseDetailFragment,
                bundleOf(
                    "productId" to productId,
                    "initialScraped" to initialScraped
                )
            )
        }
    }

    /** 상품 상세에서 뒤로가기 시 검색 결과 화면으로 복귀 */
    fun onBackFromProductDetail() {
        // NavHost 숨기기
        findViewById<View>(R.id.scrap_nav_host).visibility = View.GONE
        
        // 기존 UI 복원
        findViewById<View>(R.id.top_bar).visibility = View.VISIBLE
        findViewById<View>(R.id.search_bar).visibility = View.VISIBLE
        tabLayout.visibility = View.VISIBLE
        viewPager.visibility = View.VISIBLE
        
        // NavController 백스택 정리
        navController.popBackStack()
    }

    override fun onBackPressed() {
        // 상품 상세 화면이 표시 중이면 검색 결과로 복귀
        if (findViewById<View>(R.id.scrap_nav_host).visibility == View.VISIBLE) {
            onBackFromProductDetail()
        } else {
            super.onBackPressed()
        }
    }
}
