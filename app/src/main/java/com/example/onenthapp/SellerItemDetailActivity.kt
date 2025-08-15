package com.example.onenthapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.util.TokenManager
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class SellerItemDetailActivity : AppCompatActivity() {
    private lateinit var adapter: SellerItemFullAdapter
    private val memberApi = RetrofitInstance.memberApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_items)

        findViewById<MaterialToolbar>(R.id.topAppBar)
            .setNavigationOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.recyclerViewAllItems)
        rv.layoutManager = LinearLayoutManager(this) // 세로 리스트
        adapter = SellerItemFullAdapter()
        rv.adapter = adapter

        val sellerId = intent.getLongExtra("sellerId", -1L)
        val sellerName = intent.getStringExtra("sellerName").orEmpty()
        val itemType = intent.getStringExtra("itemType") ?: "sharing" // 기본값: 함께나눠요
        
        if (sellerId <= 0) { 
            finish() 
            return 
        }

        // 제목에 판매자명 표시
        if (sellerName.isNotBlank()) {
            findViewById<MaterialToolbar>(R.id.topAppBar).title = "${sellerName}의 판매 물품"
        }

        val token = TokenManager.getAccessToken()
        lifecycleScope.launch {
            try {
                val resp = if (itemType == "group-purchase") {
                    memberApi.getGroupPurchaseSellerProfile("Bearer ${token ?: ""}", sellerId)
                } else {
                    memberApi.getSellerProfile("Bearer ${token ?: ""}", sellerId)
                }
                
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val items = resp.body()!!.result.items
                    adapter.submitList(items)
                } else {
                    Toast.makeText(this@SellerItemDetailActivity, "판매 물품 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SellerItemDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
