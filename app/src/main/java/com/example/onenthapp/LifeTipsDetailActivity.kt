package com.example.onenthapp

import CommentAdapter
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.onenthapp.data.chat.ChatNameRequest
import com.example.onenthapp.feature.chat.ChatRoomActivity
import com.example.onenthapp.data.notificationboard.AddCommentToPostRequest
import com.example.onenthapp.data.post.PostDetailResponse
import com.example.onenthapp.databinding.ActivityLifeDetailsBinding
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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

        // 목록/검색에서 putExtra("postId", ...), (선택) putExtra("liked", true) 로 들어옴
        postId = intent.getLongExtra("postId", -1L)
        isLiked = intent.getBooleanExtra("liked", false)
        if (postId <= 0L) {
            Toast.makeText(this, "잘못된 게시글입니다.", Toast.LENGTH_SHORT).show()
            finish(); return
        }
        renderLike(isLiked)

        // 댓글 RV
        setupCommentsRv()
        loadComments()

        binding.ivBack.setOnClickListener { finish() }
        binding.ivShare.setOnClickListener { showSharePopup() }
        binding.btnSendComment.setOnClickListener { postComment() }
        binding.postlikeicon.setOnClickListener { toggleLike() }
        binding.ivBookmark.setOnClickListener { toggleScrap() }

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
                    Toast.makeText(
                        this@LifeTipsDetailActivity,
                        "상세 조회 실패: ${resp.code()} ${resp.errorBody()?.string()}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            } catch (e: UnknownHostException) {
                setLoading(false)
                Toast.makeText(this@LifeTipsDetailActivity, "서버 연결 불가", Toast.LENGTH_LONG).show()
                finish()
            } catch (e: SocketTimeoutException) {
                setLoading(false)
                Toast.makeText(this@LifeTipsDetailActivity, "요청 시간 초과", Toast.LENGTH_LONG).show()
                finish()
            } catch (e: HttpException) {
                setLoading(false)
                Toast.makeText(this@LifeTipsDetailActivity, "HTTP 오류: ${e.code()}", Toast.LENGTH_LONG).show()
                finish()
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(this@LifeTipsDetailActivity, "예외: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun renderLike(liked: Boolean) {
        binding.postlikeicon.setImageResource(
            if (liked) R.drawable.ic_board_like_filled else R.drawable.ic_board_like
        )
    }

    /** 하트 토글: 공감 취소는 MemberApi, 공감 등록은 NotificationboardApi 사용 */
    private fun toggleLike() {
        val token = TokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                binding.postlikeicon.isEnabled = false

                if (isLiked) {
                    // ✅ 내가 공감한 글 취소: DELETE /members/mypage/likes/{postId}
                    val res = RetrofitInstance.memberApi.cancelMyLikedPost("Bearer $token", postId)
                    if (res.isSuccessful && res.body()?.isSuccess == true && res.body()?.result?.isSuccess == true) {
                        isLiked = false
                        renderLike(false)
                        Toast.makeText(this@LifeTipsDetailActivity, "공감을 취소했어요.", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK, Intent().putExtra("unlikedPostId", postId))
                        // finish() // 원하면 즉시 목록으로
                    } else {
                        Toast.makeText(this@LifeTipsDetailActivity,
                            res.body()?.message ?: "공감 취소 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // ✅ 공감 등록: POST /post/{postId}/like  (인터셉터로 인증 붙는 구조라면 헤더 불필요)
                    val likeRes = RetrofitInstance.notificationboardApi.likepost(postId.toInt())
                    if (likeRes.isSuccessful && likeRes.body()?.isSuccess == true) {
                        isLiked = true
                        renderLike(true)
                        Toast.makeText(this@LifeTipsDetailActivity, "공감했어요.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LifeTipsDetailActivity, "공감 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@LifeTipsDetailActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.postlikeicon.isEnabled = true
            }
        }
    }

    // ✅ 북마크 아이콘 렌더
    private fun renderScrap(scrapped: Boolean) {
        binding.ivBookmark.setImageResource(
            if (scrapped) R.drawable.ic_bookmark_on else R.drawable.ic_bookmark_off
        )
    }

    // ✅ 스크랩 토글: 취소는 MyPage API(DELETE /members/mypage/scraps/{postId}), 등록은 게시판 API(POST /post/{postId}/scrap)
    private fun toggleScrap() {
        lifecycleScope.launch {
            try {
                binding.ivBookmark.isEnabled = false

                if (isScrapped) {
                    // 🔴 취소: 마이페이지 취소 API
                    val bearer = TokenManager.getAccessToken()?.let { "Bearer $it" } ?: run {
                        Toast.makeText(this@LifeTipsDetailActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val res = RetrofitInstance.memberApi.cancelMyScrapPost(bearer, postId)
                    if (res.isSuccessful && res.body()?.isSuccess == true && res.body()?.result?.isSuccess == true) {
                        isScrapped = false
                        renderScrap(false)
                        Toast.makeText(this@LifeTipsDetailActivity, "스크랩 취소했어요.", Toast.LENGTH_SHORT).show()
                        // 목록 쪽 새로고침 신호
                        setResult(RESULT_OK, Intent().putExtra("needRefresh", true))
                    } else {
                        Toast.makeText(this@LifeTipsDetailActivity, res.body()?.message ?: "스크랩 취소 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 🟢 등록: 게시판 API (NotificationboardApi)
                    // ※ 시그니처가 Int라면 toInt()로 변환하세요.
                    val res = RetrofitInstance.notificationboardApi.scrapPost(postId.toInt())
                    if (res.isSuccessful && res.body()?.isSuccess == true) {
                        isScrapped = true
                        renderScrap(true)
                        Toast.makeText(this@LifeTipsDetailActivity, "스크랩했어요.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LifeTipsDetailActivity, res.body()?.message ?: "스크랩 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@LifeTipsDetailActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.ivBookmark.isEnabled = true
            }
        }
    }


    /** 댓글 리스트 RecyclerView */
    private fun setupCommentsRv() {
        commentAdapter = CommentAdapter { action, c ->
            when (action) {
                CommentAdapter.Action.Chat -> {
                    // ✅ 서버로 채팅방 생성 요청 보내기
                    val token = TokenManager.getAccessToken()
                    if (token.isNullOrEmpty()) {
                        Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                        return@CommentAdapter
                    }

                    lifecycleScope.launch {
                        try {
                            val token = TokenManager.getAccessToken()
                            if (token.isNullOrEmpty()) {
                                Toast.makeText(this@LifeTipsDetailActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            // Retrofit 인터페이스에 맞게 파라미터 직접 전달
                            val res = RetrofitInstance.messageApi.createChatRoom(
                                targetMemberId = c.writeId,
                                chatRoomType = "TIP_SHARE"
                            )

                            if (res.isSuccessful && res.body()?.isSuccess == true) {
                                val chatRoom = res.body()!!.result!!

                                // 채팅방 화면으로 이동
                                val intent = Intent(this@LifeTipsDetailActivity, ChatRoomActivity::class.java).apply {
                                    putExtra("chatRoomId", chatRoom.chatRoomId)
                                    putExtra("peerNickname", chatRoom.chatRoomName)
                                    putExtra("targetId",c.writeId)
                                    putExtra("nickname", c.nickname)
                                }
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@LifeTipsDetailActivity, "채팅방 생성 실패", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                this@LifeTipsDetailActivity,
                                "채팅방 오류: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }}        }


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
        dialog.setContentView(view)
        dialog.setCancelable(true)

        val link = "https://yourapp.com/post/$postId"
        view.findViewById<EditText?>(R.id.shareLinkEditText)?.setText(link)
        view.findViewById<ViewGroup?>(R.id.copyButton)?.setOnClickListener {
            copyToClipboard("post_link", link)
            Toast.makeText(this, "링크가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<ViewGroup?>(R.id.closeButton)?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun copyToClipboard(label: String, text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    private fun bindDetail(d: PostDetailResponse.Detail) {
        binding.tvTitle.text = d.title
        binding.tvNickname.text = d.nickname ?: "익명"

        val pUrl = d.profileImageUrl
        if (!pUrl.isNullOrBlank()) {
            Glide.with(this).load(pUrl).circleCrop()
                .placeholder(R.drawable.profile_base)
                .error(R.drawable.profile_base)
                .into(binding.ivProfile)
        } else binding.ivProfile.setImageResource(R.drawable.profile_base)

        val timeAgo = toTimeAgo(d.createdAt)
        binding.tvMeta.text = d.regionName?.let { "$it · $timeAgo" } ?: timeAgo

        binding.tvContent.text = d.content
        binding.tvIconComment.text = d.commentCount.toString()
        binding.tvIconLike.text = d.likeCount.toString()
        binding.tvIconViews.text = "조회수 ${d.viewCount}"
        binding.tvCommentCount.text = "댓글 ${d.commentCount}"

        val urls = d.imageUrls.orEmpty().filter { it.isNotBlank() }.take(5)
        binding.photoScroll.isVisible = urls.isNotEmpty()
        val strip = binding.photoStrip
        strip.removeAllViews()
        if (urls.isNotEmpty()) {
            val tileSize = dp(155)
            val gap = dp(10)
            urls.forEachIndexed { idx, url ->
                val iv = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(tileSize, ViewGroup.LayoutParams.MATCH_PARENT).apply {
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

    private fun setLoading(loading: Boolean) {
        binding.btnSendComment.isEnabled = !loading
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun toTimeAgo(iso: String): String = try {
        val trimmed = iso.substringBefore('.')
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
