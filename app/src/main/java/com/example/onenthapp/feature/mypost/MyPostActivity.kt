package com.example.onenthapp.feature.mypost

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.viewpager2.widget.ViewPager2
import com.example.onenthapp.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MyPostActivity : AppCompatActivity() {
    companion object {
        const val SEARCH_KEY = "GLOBAL_SEARCH_QUERY"
        const val SEARCH_BUNDLE_KEY = "q"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_post)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        val adapter = MyPostPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "N분의 1" else "꿀팁 N분의 1"
        }.attach()

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        // ✅ 공용 검색창
        val searchEt = findViewById<EditText?>(R.id.search_bar_et)

        // 입력 변화 → 두 탭(Fragment)에게 즉시 브로드캐스트
        searchEt?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString().orEmpty()
                supportFragmentManager.setFragmentResult(SEARCH_KEY, bundleOf(SEARCH_BUNDLE_KEY to q))
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 키보드 검색 액션도 동일 처리(선택)
        searchEt?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val q = searchEt.text?.toString().orEmpty()
                supportFragmentManager.setFragmentResult(SEARCH_KEY, bundleOf(SEARCH_BUNDLE_KEY to q))
                true
            } else false
        }

        // 뷰페이저가 처음 붙은 직후, 현재 검색어(초기값)도 한 번 쏴주기
        val initial = searchEt?.text?.toString().orEmpty()
        supportFragmentManager.setFragmentResult(SEARCH_KEY, bundleOf(SEARCH_BUNDLE_KEY to initial))
    }
}
