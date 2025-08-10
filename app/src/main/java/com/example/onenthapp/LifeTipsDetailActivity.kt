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
import com.bumptech.glide.Glide
import com.example.onenthapp.data.post.PostDetailResponse
import com.example.onenthapp.databinding.ActivityLifeDetailsBinding
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch
import org.w3c.dom.Comment

//class LifeTipsDetailActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityLifeDetailsBinding
//    private lateinit var commentAdapter: CommentAdapter   // ✅ 댓글 어댑터 추가
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityLifeDetailsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // 전달받은 데이터
//        val title = intent.getStringExtra("title")
//        val content = intent.getStringExtra("content")
//        val timeAgo = intent.getStringExtra("timeAgo")
//        val commentCount = intent.getIntExtra("commentCount", 0)
//        val likeCount = intent.getIntExtra("likeCount", 0)
//        val viewCount = intent.getIntExtra("viewCount", 0)
//
//        // RecyclerView 설정
//        setupRecyclerView()
//
//        // 뒤로가기 버튼
//        binding.ivBack.setOnClickListener { finish() }
//
//        // 공유 버튼
//        binding.ivShare.setOnClickListener { showSharePopup() }
//    }
//
//    private fun setupRecyclerView() {
//        // 더미 댓글 데이터
//        val comments = listOf(
//            Comment("닉네임1", "댓글 내용 1", 2),
//            Comment("닉네임2", "댓글 내용 2", 5),
//            Comment("닉네임3", "댓글 내용 3", 1)
//        )
//
//        // 어댑터 연결
//        commentAdapter = CommentAdapter(comments)
//        binding.rvComments.layoutManager = LinearLayoutManager(this)
//        binding.rvComments.adapter = commentAdapter
//    }
//
//    private fun showSharePopup() {
//        val dialog = Dialog(this)
//        val view = LayoutInflater.from(this).inflate(R.layout.share_nwon_popup, null)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setContentView(view)
//        dialog.setCancelable(true)
//
//        val closeButton = view.findViewById<TextView>(R.id.closeButton)
//        val linkEditText = view.findViewById<EditText>(R.id.shareLinkEditText)
//        val copyButton = view.findViewById<ImageButton>(R.id.copyButton)
//
//        linkEditText.setText("https://yourapp.com/post/123")
//
//        copyButton.setOnClickListener {
//            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//            val clip = ClipData.newPlainText("링크 복사", linkEditText.text.toString())
//            clipboard.setPrimaryClip(clip)
//            Toast.makeText(this, "링크가 복사되었습니다.", Toast.LENGTH_SHORT).show()
//        }
//
//        closeButton.setOnClickListener { dialog.dismiss() }
//
//        val widthInPx = (347 * resources.displayMetrics.density).toInt()
//        val heightInPx = (202 * resources.displayMetrics.density).toInt()
//        dialog.window?.setLayout(widthInPx, heightInPx)
//        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//        dialog.show()
//    }
//}


class LifeTipsDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLifeDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLifeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    private fun bindDetail(d: PostDetailResponse.Detail) {
        binding.tvTitle.text = d.title
        binding.tvNickname.text = d.nickname ?: "익명"

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
                // 3장 이상이면 ivPhoto2에 "+N" 오버레이 넣고 싶으면 말해줘—바로 얹어줄게!
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

