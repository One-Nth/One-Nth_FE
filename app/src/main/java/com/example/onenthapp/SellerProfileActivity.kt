package com.example.onenthapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SellerProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_profile)
        // 필요하면 Intent로 넘어온 값 꺼내서 UI 세팅
        // val name = intent.getStringExtra("sellerName") ?: ""
    }
}

