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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.databinding.ActivityLifeDetailsBinding
import org.w3c.dom.Comment

class LifeTipsDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLifeDetailsBinding
    private lateinit var commentAdapter: CommentAdapter   // ✅ 댓글 어댑터 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLifeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 전달받은 데이터
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val timeAgo = intent.getStringExtra("timeAgo")
        val commentCount = intent.getIntExtra("commentCount", 0)
        val likeCount = intent.getIntExtra("likeCount", 0)
        val viewCount = intent.getIntExtra("viewCount", 0)

        // RecyclerView 설정
        setupRecyclerView()

        // 뒤로가기 버튼
        binding.ivBack.setOnClickListener { finish() }

        // 공유 버튼
        binding.ivShare.setOnClickListener { showSharePopup() }
    }

    private fun setupRecyclerView() {
        // 더미 댓글 데이터
        val comments = listOf(
            Comment("닉네임1", "댓글 내용 1", 2),
            Comment("닉네임2", "댓글 내용 2", 5),
            Comment("닉네임3", "댓글 내용 3", 1)
        )

        // 어댑터 연결
        commentAdapter = CommentAdapter(comments)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentAdapter
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

        linkEditText.setText("https://yourapp.com/post/123")

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
