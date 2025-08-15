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
import kotlinx.coroutines.launch
import java.util.Locale
import android.view.View
import androidx.compose.ui.semantics.text
import com.google.android.material.chip.Chip


class LifeTipsSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTipsSearchBinding
    private lateinit var adapter: LifeTipsSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipsSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바/뒤로가기
        binding.toolbarSearchResult.setNavigationOnClickListener { finish() }

        // 인텐트에서 검색어/게시판 타입 받기
        val query = intent.getStringExtra("query").orEmpty()
        val boardType = (intent.getStringExtra("boardType") ?: "life_tip")
            .uppercase(Locale.getDefault()) // "LIFE_TIP", "DISCOUNT", "RESTAURANT"
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
            
            // 지역 Chip 클릭 리스너 설정
            setupRegionChips(boardType)
        } else {
            binding.llRegionFilter.visibility = View.GONE
        }
    }

    private fun setupRegionChips(boardType: String) {
        // ChipGroup의 선택 변경 리스너 설정
        binding.cgRegionFilter.setOnCheckedChangeListener { group, checkedIds ->
            // group.checkedChipId를 사용하여 현재 선택된 ID를 가져옵니다.
            val currentSelectedChipId = group.checkedChipId // 선택된 Chip의 ID 또는 View.NO_ID

            if (currentSelectedChipId != View.NO_ID) {
                // 선택된 Chip이 있는 경우
                val selectedChip = group.findViewById<Chip>(currentSelectedChipId)
                val selectedChipText = selectedChip?.text ?: "알 수 없는 칩"
                Toast.makeText(
                    this,
                    "선택된 칩 ID: $currentSelectedChipId, 텍스트: $selectedChipText",
                    Toast.LENGTH_SHORT
                ).show()

                // TODO: 여기에 실제 regionName을 결정하고, search 함수를 호출하는 로직 추가
                // val regionName: String? = when (currentSelectedChipId) {
                //     R.id.chip_region_1 -> "REGION_1"
                //     R.id.chip_region_2 -> "REGION_2"
                //     R.id.chip_region_3 -> "REGION_3"
                //     else -> null
                // }
                //
                // if (regionName != null) {
                //     val query = intent.getStringExtra("query").orEmpty()
                //     search(boardType, query, regionName)
                // }

            } else {
                // 선택된 Chip이 없는 경우 (모든 Chip 선택 해제 시 - 예를 들어 selectionRequired=false 일 때)
                Toast.makeText(this, "선택된 칩 없음", Toast.LENGTH_SHORT).show()

                // TODO: 여기에 필터링 없이 전체 검색 결과를 다시 로드하는 로직 추가
                // val query = intent.getStringExtra("query").orEmpty()
                // search(boardType, query, null)
            }
        }
    }

    private fun filterSearchResultsByRegion(regionId: String, boardType: String) {
        // TODO: 실제 지역 ID로 검색 결과 필터링
        // 현재는 임시로 전체 검색 결과 표시
        val query = intent.getStringExtra("query").orEmpty()
        search(boardType, query)
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
