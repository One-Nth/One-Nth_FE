package com.example.onenthapp

import CommentAdapter
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.data.notificationboard.AddCommentToPostRequest
import com.example.onenthapp.databinding.ActivityLifeDetailsBinding
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide
import com.example.onenthapp.data.post.PostDetailResponse
import com.example.onenthapp.databinding.ActivityLifeDetailsBinding
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch
import org.w3c.dom.Comment

class LifeTipsDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLifeDetailsBinding
    private lateinit var commentAdapter: CommentAdapter
    private val api = RetrofitInstance.notificationboardApi


    private var postId: Int = -1
    private var isLiked = false
    private var isScrapped = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLifeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getIntExtra("postId", -1)

        // 전달받은 데이터
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
//        val timeAgo = intent.getStringExtra("timeAgo")
//        val commentCount = intent.getIntExtra("commentCount", 0)
//        val likeCount = intent.getIntExtra("likeCount", 0)
//        val viewCount = intent.getIntExtra("viewCount", 0)

        // RecyclerView 설정
        setupRecyclerView()
        loadComments()

        binding.tvTitle.text = title
        binding.tvContent.text = content

        binding.ivBack.setOnClickListener { finish() }
        binding.ivShare.setOnClickListener { showSharePopup() }
        binding.btnSendComment.setOnClickListener { postComment() }
        binding.postlikeicon.setOnClickListener { toggleLike() }
        binding.ivBookmark.setOnClickListener { toggleScrap() }
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(mutableListOf())
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentAdapter

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




    private fun postComment() {
        val content = binding.etComment.text.toString().trim()
        if (content.isEmpty()) return

        lifecycleScope.launch {
            try {
                val request = AddCommentToPostRequest(content = content)
                val res = api.addCommentToPost(postId, request)
                if (res.isSuccessful) {
                    binding.etComment.text.clear()
                    loadComments()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun deleteComment(commentId: Int) {
        lifecycleScope.launch {
            try {
                val res = api.deleteCommentFromPost(postId.toInt(), commentId)
                if (res.isSuccessful) {
                    loadComments()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadComments() {
        lifecycleScope.launch {
            try {
                val res = api.getPostComments(postId)
                if (res.isSuccessful) {
                    val commentItems = res.body()?.result ?: emptyList()

                    // CommentItem → Comment 변환
                    val comments = commentItems.map { item ->
                        Comment(
                            nickname = item.nickname,
                            content = item.content,
                            likeCount = 0 // API에 likeCount 없으므로 기본값 0
                        )
                    }

                    commentAdapter.updateComments(comments)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }





    private fun toggleLike() {
        lifecycleScope.launch {
            try {
                if (isLiked) {
                    val res = api.unlikepost(postId)
                    if (res.isSuccessful) {
                        isLiked = false
                        binding.postlikeicon.setImageResource(R.drawable.ic_board_like)
                    }
                } else {
                    val res = api.likepost(postId)
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

    private fun toggleScrap() {
        lifecycleScope.launch {
            try {
                if (isScrapped) {
                    val res = api.unscrapPost(postId)
                    if (res.isSuccessful) {
                        isScrapped = false
                        binding.ivBookmark.setImageResource(R.drawable.ic_bookmark_off)
                    }
                } else {
                    val res = api.scrapPost(postId)
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



    private fun showSharePopup() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.share_nwon_popup, null)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.setCancelable(true)

        linkEditText.setText("https://yourapp.com/post/$postId")


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
        val urls = d.imageUrls.orEmpty()
        when (urls.size) {
            0 -> binding.photoRow.visibility = View.GONE
            1 -> {
                binding.photoRow.visibility = View.VISIBLE
                binding.ivPhoto1.visibility = View.VISIBLE
                binding.ivPhoto2.visibility = View.GONE
                Glide.with(this).load(urls[0]).centerCrop().into(binding.ivPhoto1)
            }
            else -> {
                binding.photoRow.visibility = View.VISIBLE
                binding.ivPhoto1.visibility = View.VISIBLE
                binding.ivPhoto2.visibility = View.VISIBLE
                Glide.with(this).load(urls[0]).centerCrop().into(binding.ivPhoto1)
                Glide.with(this).load(urls[1]).centerCrop().into(binding.ivPhoto2)
            }
        }
    }

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

