package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.databinding.FragmentTipsLifetipsBinding
import com.example.onenthapp.data.post.TipItem
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class LifeTipsFragment : Fragment() {

    private var _binding: FragmentTipsLifetipsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LifeTipsSearchAdapter
    private val data = mutableListOf<TipItem>()   // 화면에 보여줄 누적 데이터

    // 페이징 상태 (이 API는 page=0부터 시작)
    private var page = 0
    private var isLoading = false
    private var isEnd = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTipsLifetipsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 1) 어댑터 생성: 상세로 postId만 전달
        adapter = LifeTipsSearchAdapter { tipItem ->
            val intent = Intent(requireContext(), LifeTipsDetailActivity::class.java)
                .putExtra("postId", tipItem.postId)
            startActivity(intent)
        }

        // 2) 리사이클러뷰 설정 + 무한스크롤
        binding.rvLifeTips.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@LifeTipsFragment.adapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(rv, dx, dy)
                    if (dy <= 0) return
                    val lm = rv.layoutManager as LinearLayoutManager
                    val last = lm.findLastVisibleItemPosition()
                    val total = this@LifeTipsFragment.adapter.itemCount
                    if (!isLoading && !isEnd && last >= total - 3) {
                        loadPage(next = true)
                    }
                }
            })
        }

        // 최초 로드
        loadPage(next = false)
    }

    /** 생활꿀팁 전체 목록 로드 (keyword 없이) */
    private fun loadPage(next: Boolean) {
        val token = TokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (isLoading) return
        isLoading = true

        if (!next) { // 새로고침/최초
            page = 0
            isEnd = false
            data.clear()
            adapter.submitItems(data)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.postApi.searchPosts(
                    bearer = "Bearer $token",
                    postType = "LIFE_TIP",
                    regionName = null,   // LIFE_TIP은 지역 필터 무시됨
                    keyword = null,      // 기본 화면: 전체 목록
                    page = page,
                    size = 10
                )

                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val dtos = resp.body()!!.result
                    val mapped = dtos.map { dto ->
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

                    data.addAll(mapped)
                    adapter.submitItems(data.toList())

                    // 다음 페이지 여부
                    if (mapped.isEmpty() || mapped.size < 10) {
                        isEnd = true
                    } else {
                        page += 1
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "목록 불러오기 실패: ${resp.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    /** createdAt(ISO) → "n분 전" */
    private fun toTimeAgo(iso: String): String {
        return try {
            val trimmed = iso.substringBefore('.')        // 예: 2025-08-12T16:40:50
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
