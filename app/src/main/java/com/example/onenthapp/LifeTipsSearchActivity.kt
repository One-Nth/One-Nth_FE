package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.data.post.TipItem
import com.example.onenthapp.databinding.ActivityTipsSearchBinding
import com.example.onenthapp.util.TokenManager
import com.example.onenthapp.data.map.MyRegionRepository
import com.example.onenthapp.data.map.MyRegion
import kotlinx.coroutines.launch
import java.util.Locale
import android.view.View
import com.google.android.material.chip.Chip


class LifeTipsSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTipsSearchBinding
    private lateinit var adapter: LifeTipsSearchAdapter
    private val myRegionRepo = MyRegionRepository()
    private var userRegions: List<MyRegion> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipsSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바/뒤로가기
        binding.toolbarSearchResult.setNavigationOnClickListener { finish() }

        // 인텐트에서 검색어/게시판 타입 받기
        val query = intent.getStringExtra("query").orEmpty()
        val rawBoardType = intent.getStringExtra("boardType") ?: "life_tip"
        // "cafe" → "RESTAURANT"로 매핑
        val boardType = when (rawBoardType.lowercase()) {
            "cafe" -> "RESTAURANT"
            else -> rawBoardType.uppercase(Locale.getDefault())
        } // "LIFE_TIP", "DISCOUNT", "RESTAURANT"
        binding.toolbarSearchResult.title = query

        // 지역 필터링 UI 설정 (할인/맛집 게시판에서만 표시)
        setupRegionFilter(boardType)

        // RecyclerView
        adapter = LifeTipsSearchAdapter { item ->
            val intent = Intent(this, LifeTipsDetailActivity::class.java)
                .putExtra("postId", item.postId)      // ✅ 이것만
            startActivity(intent)
        }

        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
        binding.rvSearchResults.adapter = adapter

        // API 호출
        search(boardType, query)
    }

    private fun setupRegionFilter(boardType: String) {
        // 할인/맛집 게시판에서만 지역 필터링 UI 표시
        if (boardType == "DISCOUNT" || boardType == "RESTAURANT") {
            binding.llRegionFilter.visibility = View.VISIBLE
            
            // 사용자 지역 로드 및 칩 바인딩
            loadUserRegionsAndSetupChips(boardType)
        } else {
            binding.llRegionFilter.visibility = View.GONE
        }
    }

    private fun loadUserRegionsAndSetupChips(boardType: String) {
        lifecycleScope.launch {
            try {
                userRegions = myRegionRepo.getMyRegions()
                bindRegionsToChips()
                setupRegionChips(boardType)
            } catch (e: Exception) {
                Toast.makeText(this@LifeTipsSearchActivity, "지역 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                // 오류 발생 시 기본 칩 텍스트 유지
                setupRegionChips(boardType)
            }
        }
    }

    private fun bindRegionsToChips() {
        // 최대 3개의 지역을 칩에 바인딩
        val chips = listOf(binding.chipRegion1, binding.chipRegion2, binding.chipRegion3)
        
        // 모든 칩을 숨기고 초기화
        chips.forEach { chip ->
            chip.visibility = View.GONE
            chip.text = "OO동"
            chip.isChecked = false // 처음에는 모든 칩을 선택 해제
        }
        
        // 사용자 지역을 칩에 바인딩 (최대 3개)
        userRegions.take(3).forEachIndexed { index, region ->
            val chip = chips[index]
            chip.visibility = View.VISIBLE
            chip.text = extractDong(region.regionName)
            // 처음에는 아무 칩도 선택하지 않음 (키워드 검색을 먼저 수행하기 위해)
        }
    }

    private fun extractDong(regionName: String): String {
        return regionName.split(" ").lastOrNull()?.replace("동", "동") ?: "OO동"
    }

    private fun setupRegionChips(boardType: String) {
        var lastSelectedChipId = View.NO_ID
        
        // ChipGroup의 선택 변경 리스너 설정
        binding.cgRegionFilter.setOnCheckedChangeListener { group, checkedId ->
            val query = intent.getStringExtra("query").orEmpty()
            
            if (checkedId != View.NO_ID) {
                // 칩이 선택된 경우
                if (checkedId == lastSelectedChipId) {
                    // 같은 칩을 다시 클릭한 경우 - 선택 해제하고 키워드 검색
                    group.clearCheck()
                    lastSelectedChipId = View.NO_ID
                    search(boardType, query)
                } else {
                    // 다른 칩을 선택한 경우 - 지역 기반 검색
                    lastSelectedChipId = checkedId
                    val selectedRegion = getSelectedRegion(checkedId)
                    if (selectedRegion != null) {
                        searchWithRegion(boardType, query, selectedRegion.regionName)
                    }
                }
            } else {
                // 선택된 칩이 없는 경우 - 키워드 검색
                lastSelectedChipId = View.NO_ID
                search(boardType, query)
            }
        }
        
        // 개별 칩 클릭 리스너로 토글 동작 구현
        val chips = listOf(
            binding.chipRegion1 to 0,
            binding.chipRegion2 to 1,
            binding.chipRegion3 to 2
        )
        
        chips.forEach { (chip, index) ->
            chip.setOnClickListener {
                if (chip.isChecked && lastSelectedChipId == chip.id) {
                    // 이미 선택된 칩을 다시 클릭한 경우 - 선택 해제
                    binding.cgRegionFilter.clearCheck()
                }
            }
        }
    }

    private fun getSelectedRegion(chipId: Int): MyRegion? {
        return when (chipId) {
            R.id.chip_region_1 -> userRegions.getOrNull(0)
            R.id.chip_region_2 -> userRegions.getOrNull(1)
            R.id.chip_region_3 -> userRegions.getOrNull(2)
            else -> null
        }
    }

    private fun searchWithRegion(postType: String, keyword: String, regionName: String) {
        val token = TokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.postApi.searchPosts(
                    bearer = "Bearer $token",
                    postType = postType,
                    regionName = regionName, // 지역 기반 검색
                    keyword = if (keyword.isBlank()) null else keyword,
                    page = 0,
                    size = 20
                )

                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val list = resp.body()!!.result.map { dto ->
                        TipItem(
                            postId = dto.postId,
                            title = dto.title,
                            content = dto.contentPreview,
                            timeAgo = toTimeAgo(dto.createdAt),
                            commentCount = dto.commentCount,
                            likeCount = dto.likeCount,
                            viewCount = dto.viewCount,
                            imageUrls = dto.imageUrls ?: emptyList()
                        )
                    }
                    adapter.submitItems(list)
                } else {
                    val err = resp.errorBody()?.string()
                    Toast.makeText(this@LifeTipsSearchActivity, "검색 실패: ${resp.code()} $err", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LifeTipsSearchActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun search(postType: String, keyword: String) {
        val token = TokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.postApi.searchPosts(
                    bearer = "Bearer $token",
                    postType = postType,
                    // LIFE_TIP은 regionName 무시, 필요시 null
                    regionName = null,
                    keyword = if (keyword.isBlank()) null else keyword,
                    page = 0,
                    size = 20
                )

                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val list = resp.body()!!.result.map { dto ->
                        TipItem(
                            postId = dto.postId,   // ✅ 추가 (postId 타입에 따라 toLong() 필요 없으면 제거)
                            title = dto.title,
                            content = dto.contentPreview,
                            timeAgo = toTimeAgo(dto.createdAt),
                            commentCount = dto.commentCount,
                            likeCount = dto.likeCount,
                            viewCount = dto.viewCount,
                            imageUrls = dto.imageUrls ?: emptyList()
                        )
                    }
                    adapter.submitItems(list)
                } else {
                    val err = resp.errorBody()?.string()
                    Toast.makeText(this@LifeTipsSearchActivity, "검색 실패: ${resp.code()} $err", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LifeTipsSearchActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // API 24 호환 "n분 전" 계산 (마이크로초 잘라내고 파싱)
    private fun toTimeAgo(iso: String): String {
        return try {
            val trimmed = iso.substringBefore('.') // 2025-08-10T21:07:28
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = sdf.parse(trimmed) ?: return trimmed

            val diffMs = System.currentTimeMillis() - date.time
            val mins = diffMs / 60000
            val hours = mins / 60
            val days = hours / 24

            when {
                mins < 1 -> "방금 전"
                mins < 60 -> "${mins}분 전"
                hours < 24 -> "${hours}시간 전"
                days < 7 -> "${days}일 전"
                else -> trimmed.replace('T', ' ')
            }
        } catch (_: Exception) {
            iso.substringBefore('.').replace('T', ' ')
        }
    }
}
