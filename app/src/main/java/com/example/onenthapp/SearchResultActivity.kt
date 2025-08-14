package com.example.onenthapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.data.item.ItemSearch
import com.example.onenthapp.data.item.ItemSearchRepository
import com.example.onenthapp.data.item.BookmarkRepository
import com.example.onenthapp.databinding.ActivitySearchResultBinding
import com.example.onenthapp.model.HomeTabType
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class SearchResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchResultBinding
    private lateinit var adapter: SearchResultAdapter
    private val searchRepository = ItemSearchRepository()
    private val bookmarkRepo = BookmarkRepository()
    
    private var searchKeyword: String = ""
    private var currentTab: HomeTabType = HomeTabType.BUY
    private var searchResults: List<ItemSearch> = emptyList()
    
    private fun showListUI() {
        // 어댑터 재부착 보장 및 리스트 재표시
        if (binding.rvSearchResults.adapter == null) {
            binding.rvSearchResults.adapter = adapter
        }
        adapter.submitList(searchResults)
        binding.searchNavHost.visibility = View.GONE
        binding.searchToolbar.visibility = View.VISIBLE
        binding.tabLayoutHome.visibility = View.VISIBLE
        binding.rvSearchResults.visibility = View.VISIBLE
        binding.tvNoResults.visibility = if (searchResults.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트에서 검색어 받기
        searchKeyword = intent.getStringExtra("keyword") ?: ""
        
        setupViews()
        setupRecyclerView()
        performSearch()
        // NavHost 목적지 변화에 따라 UI 토글 (상세 ↔ 목록)
        (supportFragmentManager.findFragmentById(R.id.search_nav_host) as? androidx.navigation.fragment.NavHostFragment)
            ?.navController
            ?.addOnDestinationChangedListener { _, dest, _ ->
                val isDetail = dest.id != R.id.emptyFragment
                if (isDetail) {
                    binding.searchNavHost.visibility = View.VISIBLE
                    binding.searchToolbar.visibility = View.GONE
                    binding.tabLayoutHome.visibility = View.GONE
                    binding.rvSearchResults.visibility = View.GONE
                    binding.tvNoResults.visibility = View.GONE
                } else {
                    showListUI()
                }
            }

        // 시스템 뒤로가기: 상세가 열려있으면 팝, 아니면 액티비티 종료
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navHost = supportFragmentManager.findFragmentById(R.id.search_nav_host) as? androidx.navigation.fragment.NavHostFragment
                val navController = navHost?.navController
                if (navController != null && navController.currentDestination?.id != R.id.emptyFragment) {
                    val popped = navController.popBackStack()
                    if (!popped) showListUI()
                } else {
                    // 이미 목록 상태
                    showListUI()
                    finish()
                }
            }
        })
    }

    private fun setupViews() {
        // 검색어 표시
        binding.tvSearchKeyword.text = searchKeyword
        
        // 뒤로가기 버튼
        binding.btnBackExpanded.setOnClickListener {
            finish()
        }
        
        // 검색어 삭제 버튼
        binding.btnClearSearch.setOnClickListener {
            binding.tvSearchKeyword.text = ""
            searchResults = emptyList()
            displaySearchResults(emptyList())
        }
        
        // 닫기 버튼
        binding.btnCloseExpanded.setOnClickListener {
            finish()
        }

        // 탭 레이아웃 설정
        binding.tabLayoutHome.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = when (tab.position) {
                    0 -> HomeTabType.BUY
                    1 -> HomeTabType.SHARE
                    else -> HomeTabType.BUY
                }
                // 탭 변경 시 현재 검색어로 다시 검색
                if (searchKeyword.isNotBlank()) {
                    performSearch()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = SearchResultAdapter(
            onItemClick = { item ->
            // 안전 가드: 잘못된 ID 방지
            Log.d("SearchClick", "tab=${currentTab}, itemId=${item.id}, bookmarked=${item.bookmarked}")
            if (item.id <= 0L) {
                Toast.makeText(this, "잘못된 상품입니다.", Toast.LENGTH_SHORT).show()
                return@SearchResultAdapter
            }
            // NavHost를 사용해 SearchResultActivity 내부에서 상세로 이동
            val navHost = supportFragmentManager.findFragmentById(R.id.search_nav_host) as? androidx.navigation.fragment.NavHostFragment
            if (navHost != null) {
                val navController = navHost.navController
                val dest = if (currentTab == HomeTabType.SHARE) R.id.sharingItemDetailFragment else R.id.groupPurchaseDetailFragment
                val args = Bundle().apply {
                    putLong("productId", item.id)
                    putBoolean("initialScraped", item.bookmarked)
                }
                binding.searchNavHost.visibility = View.VISIBLE
                binding.searchToolbar.visibility = View.GONE
                binding.tabLayoutHome.visibility = View.GONE
                binding.rvSearchResults.visibility = View.GONE
                navController.navigate(dest, args)
            }
        },
            onToggleBookmark = { item, before, onDone ->
                lifecycleScope.launch {
                    val ok = when (currentTab) {
                        HomeTabType.BUY -> if (before) bookmarkRepo.removePurchase(item.id) else bookmarkRepo.addPurchase(item.id)
                        HomeTabType.SHARE -> if (before) bookmarkRepo.removeSharing(item.id) else bookmarkRepo.addSharing(item.id)
                    }
                    Log.d("BookmarkToggle", "tab=${currentTab}, itemId=${item.id}, before=${before}, ok=${ok}")
                    if (ok) {
                        // 리스트의 동일 아이템만 업데이트 (null 안전)
                        val updated = searchResults.map {
                            if (it.id == item.id) it.copy(bookmarked = !before) else it
                        }
                        searchResults = updated
                        adapter.submitList(updated)
                    }
                    onDone(ok)
                }
            }
        )

        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(this@SearchResultActivity)
            adapter = this@SearchResultActivity.adapter
        }
    }

    private fun performSearch() {
        if (searchKeyword.isBlank()) {
            Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.rvSearchResults.visibility = View.GONE
        binding.tvNoResults.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val results = mutableListOf<ItemSearch>()
                val (effectiveKeyword, isCategory) = mapCategoryKeyword(searchKeyword)
                Log.d("SearchAPI", "keyword='${searchKeyword}', effective='${effectiveKeyword}', isCategory=${isCategory}, tab=${currentTab}")

                // 각 탭별로 제목 검색(/title) 우선 + 일반 검색 보강
                when (currentTab) {
                    HomeTabType.BUY -> {
                        // 1) 제목 검색 먼저 (카테고리여도 시도)
                        Log.d("SearchAPI", "call /group-purchases/title")
                        try {
                            val titleRes = searchRepository.searchGroupPurchasesByTitle(effectiveKeyword)
                            Log.d("SearchAPI", "/group-purchases/title code=${titleRes.code()} success=${titleRes.isSuccessful}")
                            if (titleRes.isSuccessful) {
                                titleRes.body()?.result?.let { results.addAll(it) }
                            }
                        } catch (e: Exception) {
                            Log.e("SearchAPI", "/group-purchases/title error", e)
                        }

                        // 2) 일반 검색 (중복 제거)
                        Log.d("SearchAPI", "call /group-purchases?keyword")
                        try {
                            val generalRes = searchRepository.searchGroupPurchases(effectiveKeyword)
                            Log.d("SearchAPI", "/group-purchases code=${generalRes.code()} success=${generalRes.isSuccessful}")
                            if (generalRes.isSuccessful) {
                                val general = generalRes.body()?.result.orEmpty()
                                val existing = results.map { it.id }.toHashSet()
                                results.addAll(general.filter { it.id !in existing })
                            }
                        } catch (e: Exception) {
                            Log.e("SearchAPI", "/group-purchases error", e)
                        }
                    }
                    HomeTabType.SHARE -> {
                        // 1) 제목 검색 먼저 (카테고리여도 시도)
                        Log.d("SearchAPI", "call /sharing-items/title")
                        try {
                            val titleRes = searchRepository.searchSharingItemsByTitle(effectiveKeyword)
                            Log.d("SearchAPI", "/sharing-items/title code=${titleRes.code()} success=${titleRes.isSuccessful}")
                            if (titleRes.isSuccessful) {
                                titleRes.body()?.result?.let { results.addAll(it) }
                            }
                        } catch (e: Exception) {
                            Log.e("SearchAPI", "/sharing-items/title error", e)
                        }

                        // 2) 일반 검색 (중복 제거)
                        Log.d("SearchAPI", "call /sharing-items?keyword")
                        try {
                            val generalRes = searchRepository.searchSharingItems(effectiveKeyword)
                            Log.d("SearchAPI", "/sharing-items code=${generalRes.code()} success=${generalRes.isSuccessful}")
                            if (generalRes.isSuccessful) {
                                val general = generalRes.body()?.result.orEmpty()
                                val existing = results.map { it.id }.toHashSet()
                                results.addAll(general.filter { it.id !in existing })
                            }
                        } catch (e: Exception) {
                            Log.e("SearchAPI", "/sharing-items error", e)
                        }
                    }
                }
                // id <= 0 필터링 (상세 0 호출 방지)
                val filtered = results.filter { it.id > 0L }
                if (filtered.size != results.size) {
                    Log.w("SearchAPI", "filteredOutInvalidIds=${results.size - filtered.size}")
                }
                Log.d("SearchAPI", "resultCount=${filtered.size}")
                searchResults = filtered
                displaySearchResults(filtered)
                
            } catch (e: Exception) {
                Toast.makeText(this@SearchResultActivity, "검색 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun mapCategoryKeyword(raw: String): Pair<String, Boolean> {
        val trimmed = raw.trim()
        val mapping = mapOf(
            "전자제품" to "ELECTRONICS",
            "생활용품" to "HOUSEHOLD",
            "식품" to "FOOD",
            "의류" to "CLOTHING",
            "잡화" to "MISC",
            // 영문 그대로 들어온 경우도 통과
            "ELECTRONICS" to "ELECTRONICS",
            "HOUSEHOLD" to "HOUSEHOLD",
            "FOOD" to "FOOD",
            "CLOTHING" to "CLOTHING",
            "MISC" to "MISC"
        )
        val mapped = mapping[trimmed]
        return if (mapped != null) mapped to true else trimmed to false
    }
    private fun displaySearchResults(results: List<ItemSearch>) {
        if (results.isEmpty()) {
            binding.tvNoResults.visibility = View.VISIBLE
            binding.rvSearchResults.visibility = View.GONE
        } else {
            binding.tvNoResults.visibility = View.GONE
            binding.rvSearchResults.visibility = View.VISIBLE
            adapter.submitList(results)
        }
    }
}
