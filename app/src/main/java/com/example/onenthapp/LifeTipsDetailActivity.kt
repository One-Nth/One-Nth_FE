package com.example.onenthapp

import CommentAdapter
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.onenthapp.chat.ChatRoomActivity
import com.example.onenthapp.data.notificationboard.AddCommentToPostRequest
import com.example.onenthapp.data.post.PostDetailResponse
import com.example.onenthapp.databinding.ActivityLifeDetailsBinding
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch

class LifeTipsDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLifeDetailsBinding
    private lateinit var commentAdapter: CommentAdapter
    private val api = RetrofitInstance.notificationboardApi

    private var postId: Long = -1L
    private var isLiked = false
    private var isScrapped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLifeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // postId만 받으면 됩니다(목록/검색에서 putExtra("postId", ...))
        postId = intent.getLongExtra("postId", -1L)
        if (postId <= 0L) {
            Toast.makeText(this, "잘못된 게시글입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 댓글 RV
        setupCommentsRv()
        loadComments()

        // 클릭 리스너
        binding.ivBack.setOnClickListener { finish() }
        binding.ivShare.setOnClickListener { showSharePopup() }
        binding.btnSendComment.setOnClickListener { postComment() }
        binding.postlikeicon.setOnClickListener { toggleLike() }
        binding.ivBookmark.setOnClickListener { toggleScrap() }

        // 상세 API
        val token = TokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.postApi.getPostDetail("Bearer $token", postId)
                setLoading(false)
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    resp.body()!!.result?.let { bindDetail(it) }
                } else {
                    Toast.makeText(
                        this@LifeTipsDetailActivity,
                        "상세 조회 실패: ${resp.code()} ${resp.errorBody()?.string()}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(this@LifeTipsDetailActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /** 댓글 리스트 RecyclerView */
    private fun setupCommentsRv() {
        commentAdapter = CommentAdapter { action, c ->
            when (action) {
                CommentAdapter.Action.Chat -> {
                    // ✅ ChatRoomActivity로 이동
                    val intent = Intent(this, ChatRoomActivity::class.java).apply {
                        // 나중에 서버에서 writerId 내려주면 아래처럼 같이 넘기면 됨
                        // putExtra("peerId", c.writerId)
                        putExtra("peerNickname", c.nickname)   // 선택
                        putExtra("fromPostId", postId)          // 선택
                    }
                    startActivity(intent)
                }
                CommentAdapter.Action.Block ->
                    Toast.makeText(this, "차단: ${c.nickname}", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@LifeTipsDetailActivity)
            adapter = commentAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }
    }



    /** 댓글 작성 */
    private fun postComment() {
        val content = binding.etComment.text.toString().trim()
        if (content.isEmpty()) return

        lifecycleScope.launch {
            try {
                val request = AddCommentToPostRequest(content = content)
                val res = api.addCommentToPost(postId.toInt(), request)
                if (res.isSuccessful) {
                    binding.etComment.text.clear()
                    loadComments()
                } else {
                    Toast.makeText(this@LifeTipsDetailActivity, "댓글 등록 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@LifeTipsDetailActivity, "댓글 등록 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 댓글 삭제(필요 시 사용) */
    private fun deleteComment(commentId: Int) {
        lifecycleScope.launch {
            try {
                val res = api.deleteCommentFromPost(postId.toInt(), commentId)
                if (res.isSuccessful) {
                    loadComments()
                } else {
                    Toast.makeText(this@LifeTipsDetailActivity, "댓글 삭제 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@LifeTipsDetailActivity, "댓글 삭제 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 댓글 목록 로드 */
    private fun loadComments() {
        lifecycleScope.launch {
            try {
                val res = api.getPostComments(postId.toInt())
                if (res.isSuccessful) {
                    val items = res.body()?.result ?: emptyList()
                    val comments = items.map { item ->
                        // 프로젝트 내 정의된 Comment 데이터클래스를 사용하세요 (org.w3c.dom.Comment 말고!)
                        Comment(
                            nickname = item.nickname,
                            content = item.content,
                            likeCount = 0
                        )
                    }
                    commentAdapter.submitList(comments)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** 좋아요 토글 */
    private fun toggleLike() {
        lifecycleScope.launch {
            try {
                if (isLiked) {
                    val res = api.unlikepost(postId.toInt())
                    if (res.isSuccessful) {
                        isLiked = false
                        binding.postlikeicon.setImageResource(R.drawable.ic_board_like)
                    }
                } else {
                    val res = api.likepost(postId.toInt())
                    if (res.isSuccessful) {
                        isLiked = true
                        binding.postlikeicon.setImageResource(R.drawable.ic_board_like_filled)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** 스크랩 토글 */
    private fun toggleScrap() {
        lifecycleScope.launch {
            try {
                if (isScrapped) {
                    val res = api.unscrapPost(postId.toInt())
                    if (res.isSuccessful) {
                        isScrapped = false
                        binding.ivBookmark.setImageResource(R.drawable.ic_bookmark_off)
                    }
                } else {
                    val res = api.scrapPost(postId.toInt())
                    if (res.isSuccessful) {
                        isScrapped = true
                        binding.ivBookmark.setImageResource(R.drawable.ic_bookmark_on)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** 공유 팝업 */
    private fun showSharePopup() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.share_nwon_popup, null)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.setCancelable(true)

        val link = "https://yourapp.com/post/$postId"
        view.findViewById<EditText?>(R.id.shareLinkEditText)?.setText(link)
        view.findViewById<View?>(R.id.copyButton)?.setOnClickListener {
            copyToClipboard("post_link", link)
            Toast.makeText(this, "링크가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View?>(R.id.closeButton)?.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun copyToClipboard(label: String, text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    /** 상세 바인딩 */
    private fun bindDetail(d: PostDetailResponse.Detail) {
        binding.tvTitle.text = d.title
        binding.tvNickname.text = d.nickname ?: "익명"

        // 프로필
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

        // 메타(지역명 없으면 시간만)
        val timeAgo = toTimeAgo(d.createdAt)
        binding.tvMeta.text = d.regionName?.let { "$it · $timeAgo" } ?: timeAgo

        binding.tvContent.text = d.content

        // 아이콘 줄 카운트
        binding.tvIconComment.text = d.commentCount.toString()
        binding.tvIconLike.text = d.likeCount.toString()
        binding.tvIconViews.text = "조회수 ${d.viewCount}"

        // 하단 "댓글 N"
        binding.tvCommentCount.text = "댓글 ${d.commentCount}"

        // 이미지 스트립 (최대 5장)
        val urls = d.imageUrls.orEmpty().filter { it.isNotBlank() }.take(5)
        binding.photoScroll.isVisible = urls.isNotEmpty()
        val strip = binding.photoStrip
        strip.removeAllViews()
        if (urls.isNotEmpty()) {
            val tileSize = dp(155)   // 높이와 동일한 정사각형
            val gap = dp(10)
            urls.forEachIndexed { idx, url ->
                val iv = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        tileSize,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ).apply {
                        if (idx != urls.lastIndex) marginEnd = gap
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setBackgroundResource(R.drawable.rectangle_tips)
                }
                Glide.with(this).load(url).into(iv)
                strip.addView(iv)
            }
        }
    }

    /** 로딩 상태 처리(필요 시 ProgressBar 연결) */
    private fun setLoading(loading: Boolean) {
        // binding.progress.isVisible = loading  // 레이아웃에 있으면 사용
        binding.btnSendComment.isEnabled = !loading
    }

    /** dp → px */
    private fun dp(v: Int): Int =
        (v * resources.displayMetrics.density).toInt()

    /** ISO → "n분 전" */
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
