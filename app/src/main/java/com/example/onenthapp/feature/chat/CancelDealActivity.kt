package com.example.onenthapp.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.onenthapp.R

import android.content.Intent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import com.example.onenthapp.MyReviewActivity
import com.example.onenthapp.NwonSavedActivity

class CancelDealActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel_deal)

        val roomName = intent.getStringExtra("roomName")
        val isWriter = intent.getBooleanExtra("isWriter", false)  // 추가: 작성자 여부 받기

        // 작성자면 "판매자 측 과실" 레이아웃 숨기기
        val dealWriteLayout = findViewById<LinearLayout>(R.id.deal_write)
        if (isWriter) {
            dealWriteLayout.visibility = View.GONE
        } else {
            dealWriteLayout.visibility = View.VISIBLE
        }

        findViewById<ImageButton>(R.id.btn_review).setOnClickListener {
            val intent = Intent(this, MyReviewActivity::class.java)
            intent.putExtra("roomName", roomName)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btn_savings).setOnClickListener {
            val intent = Intent(this, NwonSavedActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btn_left).setOnClickListener {
            finish()
        }
    }
}


