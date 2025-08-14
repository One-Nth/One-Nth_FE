package com.example.onenthapp.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.onenthapp.R

import android.content.Intent
import android.widget.ImageButton
import com.example.onenthapp.MyReviewActivity
import com.example.onenthapp.NwonSavedActivity

class CancelDealActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel_deal)

        val roomName = intent.getStringExtra("roomName")

        // 후기 남기기 버튼 클릭 시
        findViewById<ImageButton>(R.id.btn_review).setOnClickListener {
            val intent = Intent(this, MyReviewActivity::class.java)
            intent.putExtra("roomName", roomName) // 필요 시 전달
            startActivity(intent)
        }

        // N원 아꼈어요 보기 버튼 클릭 시
        findViewById<ImageButton>(R.id.btn_savings).setOnClickListener {
            val intent = Intent(this, NwonSavedActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btn_left).setOnClickListener {
            finish()
        }
    }
}

