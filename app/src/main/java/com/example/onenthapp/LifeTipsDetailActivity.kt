package com.example.onenthapp

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
import com.example.onenthapp.databinding.ActivityLifeDetailsBinding

class LifeTipsDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLifeDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLifeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 전달받은 데이터 받아오기
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val timeAgo = intent.getStringExtra("timeAgo")
        val commentCount = intent.getIntExtra("commentCount", 0)
        val likeCount = intent.getIntExtra("likeCount", 0)
        val viewCount = intent.getIntExtra("viewCount", 0)

        // 텍스트뷰에 설정
//        binding.tvTitle.text = title
//        binding.tvContent.text = content
//        binding.tvTime.text = timeAgo
//        binding.tvComment.text = "댓글 $commentCount"
//        binding.tvLike.text = "좋아요 $likeCount"
//        binding.tvViews.text = "조회수 $viewCount"

        // 뒤로가기 버튼
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 공유 버튼
        binding.ivShare.setOnClickListener {
            showSharePopup()
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

        // 샘플 링크 설정
        linkEditText.setText("https://yourapp.com/post/123") // 실제 URL로 교체 가능

        // 복사 버튼 클릭 처리
        copyButton.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("링크 복사", linkEditText.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "링크가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }

        // 닫기 버튼 처리
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        // 팝업 크기 조정 (화면 너비 90%)
        val widthInPx = (347 * resources.displayMetrics.density).toInt()
        val heightInPx = (202 * resources.displayMetrics.density).toInt()
        dialog.window?.setLayout(widthInPx, heightInPx)

        // 배경 투명화 (둥근 테두리 유지)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()
    }
}