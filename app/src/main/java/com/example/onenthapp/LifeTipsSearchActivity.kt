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



class LifeTipsSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTipsSearchBinding
    private lateinit var adapter: LifeTipsSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipsSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바/뒤로가기
        binding.ivBack.setOnClickListener { finish() }

        // 인텐트에서 검색어/게시판 타입 받기
        val query = intent.getStringExtra("query").orEmpty()
        val boardType = (intent.getStringExtra("boardType") ?: "life_tip")
            .uppercase(Locale.getDefault()) // "LIFE_TIP", "DISCOUNT", "RESTAURANT"
        binding.tvSearchKeyword.text = query

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
