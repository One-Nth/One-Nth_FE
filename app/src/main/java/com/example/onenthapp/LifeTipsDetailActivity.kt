package com.example.onenthapp

import CommentAdapter
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.onenthapp.data.notificationboard.AddCommentToPostRequest
import com.example.onenthapp.data.post.PostDetailResponse
import com.example.onenthapp.databinding.ActivityLifeDetailsBinding
import com.example.onenthapp.feature.chat.ChatRoomActivity
import com.example.onenthapp.util.TokenManager
import com.example.onenthapp.utils.ShareDialogUtil
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class LifeTipsDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLifeDetailsBinding
    private lateinit var commentAdapter: CommentAdapter
    private val api = RetrofitInstance.notificationboardApi

    private var postId: Long = -1L

    // ✅ 상태: 내가 공감/스크랩 했는지 (마이페이지에서 온 경우 취소 허용 기준)
    private var isLiked = false
    private var isScrapped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLifeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 진입 파라미터
        postId = intent.getLongExtra("postId", -1L)
        isLiked = intent.getBooleanExtra("liked", false)
        isScrapped = intent.getBooleanExtra("scrapped", false) // ← 마이페이지에서 오면 같이 넣어줘

        if (postId <= 0L) {
            Toast.makeText(this, "잘못된 게시글입니다.", Toast.LENGTH_SHORT).show()
            finish(); return
        }
        // ✅ 진입할 때 서버 기준으로 하트/스크랩 상태 동기화
        refreshMyActions()
        fetchDetailAndBind()
        renderLike(isLiked)
        renderScrap(isScrapped) // ✅ 초기 렌더

        // 댓글 목록 세팅
        setupCommentsRv()
        loadComments()


        // 상단/액션
        binding.ivBack.setOnClickListener { finish() }
        binding.ivShare.setOnClickListener { showSharePopup() }
        binding.btnSendComment.setOnClickListener { postComment() }

        // ✅ 토글 연결
        binding.postlikeicon.setOnClickListener { toggleLike() }
        binding.ivBookmark.setOnClickListener { toggleScrap() }

//        val token = TokenManager.getAccessToken()
//        if (token.isNullOrEmpty()) {
//            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
//            finish(); return
//        }
//
//        setLoading(true)
//        lifecycleScope.launch {
//            try {
//                val resp = RetrofitInstance.postApi.getPostDetail("Bearer $token", postId)
//                setLoading(false)
//                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
//                    resp.body()!!.result?.let { bindDetail(it) }
//
//                    // ✅ 서버가 내가 공감/스크랩했는지 알려주면 여기서 갱신 (필드 있으면 사용, 없으면 무시)
//                    // 예: if (it.likedByMe != null) { isLiked = it.likedByMe; renderLike(isLiked) }
//                    // 예: if (it.scrappedByMe != null) { isScrapped = it.scrappedByMe; renderScrap(isScrapped) }
//                } else {
//                    Toast.makeText(
//                        this@LifeTipsDetailActivity,
//                        "상세 조회 실패: ${resp.code()} ${resp.errorBody()?.string()}",
//                        Toast.LENGTH_LONG
//                    ).show()
//                    finish()
//                }
//            } catch (e: UnknownHostException) {
//                setLoading(false)
//                Toast.makeText(this@LifeTipsDetailActivity, "서버 연결 불가", Toast.LENGTH_LONG).show()
//                finish()
//            } catch (e: SocketTimeoutException) {
//                setLoading(false)
//                Toast.makeText(this@LifeTipsDetailActivity, "요청 시간 초과", Toast.LENGTH_LONG).show()
//                finish()
//            } catch (e: HttpException) {
//                setLoading(false)
//                Toast.makeText(this@LifeTipsDetailActivity, "HTTP 오류: ${e.code()}", Toast.LENGTH_LONG).show()
//                finish()
//            } catch (e: Exception) {
//                setLoading(false)
//                Toast.makeText(this@LifeTipsDetailActivity, "예외: ${e.message}", Toast.LENGTH_LONG).show()
//                finish()
//            }
//        }
    }

    // ==================== 공감 ====================

    private fun renderLike(liked: Boolean) {
        binding.postlikeicon.setImageResource(
            if (liked) R.drawable.ic_board_like_filled else R.drawable.ic_board_like
        )
    }

    private fun toggleLike() {
        lifecycleScope.launch {
            try {
                binding.postlikeicon.isEnabled = false
                val api = RetrofitInstance.notificationboardApi

                if (isLiked) {
                    // 취소
                    val res = api.unlikepost(postId.toInt())
                    if (res.isSuccessful && res.body()?.result?.isSuccess == true) {
                        isLiked = false
                        // 개수 -1 (음수 방지)
                        val cur = binding.tvIconLike.text.toString().toIntOrNull() ?: 0
                        binding.tvIconLike.text = maxOf(0, cur - 1).toString()
                        renderLike(false)
                        Toast.makeText(this@LifeTipsDetailActivity, "공감을 취소했어요.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LifeTipsDetailActivity, res.body()?.message ?: "공감 취소 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 등록
                    val res = api.likepost(postId.toInt())
                    if (res.isSuccessful && res.body()?.result?.isSuccess == true) {
                        isLiked = true
                        // 개수 +1
                        val cur = binding.tvIconLike.text.toString().toIntOrNull() ?: 0
                        binding.tvIconLike.text = (cur + 1).toString()
                        renderLike(true)
                        Toast.makeText(this@LifeTipsDetailActivity, "공감했어요.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LifeTipsDetailActivity, res.body()?.message ?: "공감 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                refreshMyActions()

                // 목록/탭 새로고침용 결과 전달
                setResult(RESULT_OK, Intent().apply {
                    putExtra("postId", postId)
                    putExtra("likeChanged", true)
                    putExtra("liked", isLiked)
                })

            } catch (e: Exception) {
                Toast.makeText(this@LifeTipsDetailActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.postlikeicon.isEnabled = true
            }
        }
    }


    // ✅ 공감 수 증감 도우미
    private fun bumpLikeCount(delta: Int) {
        val cur = binding.tvIconLike.text.toString().toIntOrNull() ?: 0
        val next = (cur + delta).coerceAtLeast(0)
        binding.tvIconLike.text = next.toString()
    }

    // ==================== 스크랩 ====================

    private fun renderScrap(scrapped: Boolean) {
        binding.ivBookmark.setImageResource(
            if (scrapped) R.drawable.ic_bookmark_on else R.drawable.ic_bookmark_off
        )
    }

    private fun toggleScrap() {
        lifecycleScope.launch {
            try {
                binding.ivBookmark.isEnabled = false
                val api = RetrofitInstance.notificationboardApi

                if (isScrapped) {
                    // 취소
                    val res = api.unscrapPost(postId.toInt())
                    if (res.isSuccessful && res.body()?.result?.isSuccess == true) {
                        isScrapped = false
                        renderScrap(false)
                        Toast.makeText(this@LifeTipsDetailActivity, "스크랩을 취소했어요.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LifeTipsDetailActivity, res.body()?.message ?: "스크랩 취소 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 등록
                    val res = api.scrapPost(postId.toInt())
                    if (res.isSuccessful && res.body()?.result?.isSuccess == true) {
                        isScrapped = true
                        renderScrap(true)
                        Toast.makeText(this@LifeTipsDetailActivity, "스크랩했어요.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LifeTipsDetailActivity, res.body()?.message ?: "스크랩 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                refreshMyActions()

                // 목록/탭 새로고침용 결과 전달
                setResult(RESULT_OK, Intent().apply {
                    putExtra("postId", postId)
                    putExtra("scrapChanged", true)
                    putExtra("scrapped", isScrapped)
                })

            } catch (e: Exception) {
                Toast.makeText(this@LifeTipsDetailActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.ivBookmark.isEnabled = true
            }
        }
    }


    // ==================== 댓글 ====================

    private fun setupCommentsRv() {
        commentAdapter = CommentAdapter { action, c ->
            when (action) {
                CommentAdapter.Action.Chat -> {
                    val token = TokenManager.getAccessToken()
                    if (token.isNullOrEmpty()) {
                        Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                        return@CommentAdapter
                    }
                    lifecycleScope.launch {
                        try {
                            val res = RetrofitInstance.messageApi.createChatRoom(
                                targetMemberId = c.writeId,
                                chatRoomType = "TIP_SHARE"
                            )
                            if (res.isSuccessful && res.body()?.isSuccess == true) {
                                val chatRoom = res.body()!!.result!!
                                val intent = Intent(this@LifeTipsDetailActivity, ChatRoomActivity::class.java).apply {
                                    putExtra("chatRoomId", chatRoom.chatRoomId)
                                    putExtra("myMemberId", TokenManager.getMemberId())
                                    putExtra("roomName", chatRoom.chatRoomName)
                                    putExtra("peerNickname", c.nickname)
                                    putExtra("opponentId", c.writeId)
                                }
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@LifeTipsDetailActivity, "채팅방 생성 실패", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this@LifeTipsDetailActivity, "채팅방 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                CommentAdapter.Action.Block -> {
                    val token = TokenManager.getAccessToken()
                    if (token.isNullOrEmpty()) {
                        Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                        return@CommentAdapter
                    }
                    lifecycleScope.launch {
                        try {
                            val res = RetrofitInstance.messageApi.blockMember(c.writeId)
                            if (res.isSuccessful && res.body()?.isSuccess == true) {
                                Toast.makeText(this@LifeTipsDetailActivity, "${c.nickname}님을 차단하였습니다.", Toast.LENGTH_SHORT).show()
                                loadComments()
                            } else {
                                Toast.makeText(this@LifeTipsDetailActivity, "차단 실패", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@LifeTipsDetailActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@LifeTipsDetailActivity)
            adapter = commentAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }
    }

    private fun postComment() {
        val content = binding.etComment.text.toString().trim()
        if (content.isEmpty()) return

        lifecycleScope.launch {
            try {
                val res = api.addCommentToPost(postId.toInt(), AddCommentToPostRequest(content))
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

    private fun loadComments() {
        lifecycleScope.launch {
            try {
                val res = api.getPostComments(postId.toInt())
                if (res.isSuccessful) {
                    val items = res.body()?.result ?: emptyList()
                    val comments = items.map { item ->
                        // 앱 내부 Comment 데이터 클래스를 사용 (org.w3c.dom.Comment 아님)
                        Comment(
                            nickname = item.nickname,
                            content = item.content,
                            likeCount = 0,
                            writeId = item.memberId
                        )
                    }
                    commentAdapter.submitList(comments)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==================== 상세 바인딩 ====================

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

        // 사진 스트립
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

    // ==================== 공용 ====================

    private fun showSharePopup() {
        val shareUrl = "https://onenthapp.com/post/$postId"
        ShareDialogUtil.showShareDialog(this, shareUrl, "해당 글을 공유하시겠습니까?")
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSendComment.isEnabled = !loading
        // 필요하면 다른 버튼도 디세이블 처리 가능
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

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

    private fun fetchDetailAndBind() {
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
                    val detail = resp.body()!!.result
                    if (detail == null) {
                        Toast.makeText(this@LifeTipsDetailActivity, "게시글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        finish(); return@launch
                    }

                    // UI 바인딩
                    bindDetail(detail)
                    refreshMyActions()
                    // ✅ 초기 하트/스크랩 상태 렌더링
                    // 서버가 myLiked/myScrapped 같은 값 내려주면 여기서 세팅 (필드 생기면 주석을 해제하세요)
                    // isLiked = detail.myLiked ?: isLiked
                    // isScrapped = detail.myScrapped ?: isScrapped

                    // 목록/검색에서 putExtra로 넘어온 값이 있으면 우선 적용(없으면 기존값 유지)
                    renderLike(isLiked)
                    renderScrap(isScrapped)

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
                Toast.makeText(this@LifeTipsDetailActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun refreshMyActions() {
        lifecycleScope.launch {
            try {
                // 내 공감/스크랩 목록 가져오기 (Auth 인터셉터가 붙는 구조라면 헤더 불필요)
                val likedRes = RetrofitInstance.memberApi.getMyLikedPosts(page = 1, size = 100)
                val scrapRes = RetrofitInstance.memberApi.getMyScrapPosts(page = 1, size = 100)

                // ✅ 네 모델에 맞춰서 postList 사용
                val likedList  = likedRes.body()?.result?.postList ?: emptyList()
                val scrapedList = scrapRes.body()?.result?.postList ?: emptyList()

                // ✅ 아이디 비교는 postId
                isLiked    = likedList.any   { it.postId == postId }
                isScrapped = scrapedList.any { it.postId == postId }

                // UI 반영
                renderLike(isLiked)
                renderScrap(isScrapped)
            } catch (_: Exception) {
                // 네트워크 실패 시엔 기존 상태 유지 (원하면 Toast 추가)
            }
        }
    }




}
