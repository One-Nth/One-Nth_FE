package com.example.onenthapp

import CommentAdapter
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
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
    }


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

        val closeButton = view.findViewById<TextView>(R.id.closeButton)
        val linkEditText = view.findViewById<EditText>(R.id.shareLinkEditText)
        val copyButton = view.findViewById<ImageButton>(R.id.copyButton)

        linkEditText.setText("https://yourapp.com/post/$postId")

        copyButton.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("링크 복사", linkEditText.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "링크가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }

        closeButton.setOnClickListener { dialog.dismiss() }

        val widthInPx = (347 * resources.displayMetrics.density).toInt()
        val heightInPx = (202 * resources.displayMetrics.density).toInt()
        dialog.window?.setLayout(widthInPx, heightInPx)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }



}
