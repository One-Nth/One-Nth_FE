package com.example.onenthapp

import CommentAdapter
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.onenthapp.data.post.PostDetailResponse
import com.example.onenthapp.databinding.ActivityLifeDetailsBinding
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch
import org.w3c.dom.Comment

class LifeTipsDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLifeDetailsBinding
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLifeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 댓글  ---------------------------------------------------------
        // 댓글 리사이클러뷰 세팅
        setupCommentsRv()
        // 🔹더미 댓글로 팝업 테스트
        showDummyComments()
        // 댓글  ---------------------------------------------------------


        binding.ivBack.setOnClickListener { finish() }

        val postId = intent.getLongExtra("postId", -1L)
        if (postId <= 0L) {
            Toast.makeText(this, "잘못된 게시글입니다.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        val token = TokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.postApi.getPostDetail("Bearer $token", postId)
                setLoading(false)

                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    resp.body()!!.result?.let { bindDetail(it) }
                } else {
                    val err = resp.errorBody()?.string()
                    Toast.makeText(this@LifeTipsDetailActivity, "상세 조회 실패: ${resp.code()} $err", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(this@LifeTipsDetailActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }



    // 댓글  ---------------------------------------------------------
    private fun setupCommentsRv() {
        commentAdapter = CommentAdapter { action, c ->
            when (action) {
                CommentAdapter.Action.Chat  -> Toast.makeText(this, "채팅: ${c.nickname}", Toast.LENGTH_SHORT).show()
                CommentAdapter.Action.Block -> Toast.makeText(this, "차단: ${c.nickname}", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@LifeTipsDetailActivity)
            adapter = commentAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }
    }

    private fun showDummyComments() {
        val dummy = listOf(
            Comment(nickname = "asdds", content = "저 여름마다 잘 쓰고 있어요", likeCount = 2),
            Comment(nickname = "bt26az", content = "사진 첨부합니다~", likeCount = 0),
        )
        commentAdapter.submitList(dummy)
    }
    // 댓글  ---------------------------------------------------------





    private fun bindDetail(d: PostDetailResponse.Detail) {
        binding.tvTitle.text = d.title
        binding.tvNickname.text = d.nickname ?: "익명"

        // 프로필 이미지
        val pUrl = d.profileImageUrl
        if (!pUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(pUrl)
                .circleCrop()
                .placeholder(R.drawable.profile_base)
                .error(R.drawable.profile_base)
                .into(binding.ivProfile)
        } else {
            binding.ivProfile.setImageResource(R.drawable.profile_base)
        }

        val timeAgo = toTimeAgo(d.createdAt)
        // LIFE_TIP은 regionName=null → 시간만 보여주기
        binding.tvMeta.text = d.regionName?.let { "$it · $timeAgo" } ?: timeAgo

        binding.tvContent.text = d.content

        // 아이콘 줄 카운트
        binding.tvIconComment.text = d.commentCount.toString()
        binding.tvIconLike.text = d.likeCount.toString()
        binding.tvIconViews.text = "조회수 ${d.viewCount}"

        // 하단 "댓글 N" 텍스트도 갱신
        binding.tvCommentCount.text = "댓글 ${d.commentCount}"

        // 이미지 0~2장 처리

        val urls = d.imageUrls.orEmpty().filter { it.isNotBlank() }.take(5)

        if (urls.isEmpty()) {
            binding.photoScroll.visibility = View.GONE
        } else {
            binding.photoScroll.visibility = View.VISIBLE
            val strip = binding.photoStrip
            strip.removeAllViews()

            val tileSize = dp(155)          // 카드 높이와 동일(정사각형 느낌). 필요시 수정
            val gap = dp(10)                // 카드 사이 간격

            urls.forEachIndexed { idx, url ->
                val iv = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(tileSize, ViewGroup.LayoutParams.MATCH_PARENT).apply {
                        if (idx != urls.lastIndex) marginEnd = gap
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP   // 카드 내부는 꽉 채움(화면 전체X)
                    setBackgroundResource(R.drawable.rectangle_tips) // 기존 둥근 모서리 배경
                }
                Glide.with(this).load(url).into(iv)
                strip.addView(iv)
            }
        }
    }

    private fun dp(v: Int): Int =
        (v * resources.displayMetrics.density).toInt()


    private fun setLoading(loading: Boolean) {
        // 필요시 ProgressBar 제어
        // binding.progress.isVisible = loading
    }

    // API 24 호환 "n분 전"
    private fun toTimeAgo(iso: String): String = try {
        val trimmed = iso.substringBefore('.') // 2025-08-10T21:07:28
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(trimmed) ?: return trimmed

        val diffMs = System.currentTimeMillis() - date.time
        val mins = diffMs / 60000
        val hours = mins / 60
        val days = hours / 24

        when {
            mins < 60 -> "${mins}분 전"
            hours < 24 -> "${hours}시간 전"
            days < 7 -> "${days}일 전"
            else -> trimmed.replace('T', ' ')
        }
    } catch (_: Exception) {
        iso.substringBefore('.').replace('T', ' ')
    }
}

