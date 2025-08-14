package com.example.onenthapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ScrapActivity : AppCompatActivity() {

    companion object {
        const val SEARCH_KEY = "GLOBAL_SEARCH_QUERY"
        const val SEARCH_BUNDLE_KEY = "q"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrap)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

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
}
