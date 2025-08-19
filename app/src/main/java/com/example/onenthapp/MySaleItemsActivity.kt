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

class MySaleItemsActivity : AppCompatActivity() {
    private lateinit var adapter: SellerItemAdapter
    private val memberApi = RetrofitInstance.memberApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_sale_items)

        findViewById<MaterialToolbar>(R.id.topAppBar)
            .setNavigationOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.recyclerViewMySaleItems)
        rv.layoutManager = LinearLayoutManager(this) // 세로 리스트
        adapter = SellerItemAdapter(hideCategory = true)
        rv.adapter = adapter

        loadMySaleItems()
    }

    private fun loadMySaleItems() {
        val token = TokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val response = memberApi.getMyItems("Bearer ${token ?: ""}", page = 1, size = 50)
                if (response.isSuccess && response.result != null) {
                    // MyPostProductItem을 SellerItem으로 변환
                    val sellerItems = response.result.items.map { item ->
                        com.example.onenthapp.data.SellerItem(
                            id = item.itemId,
                            name = item.productName,
                            status = "DEFAULT", // MyPostProductItem에는 status가 없으므로 기본값
                            price = item.price.toInt(),
                            itemCategory = item.itemType, // PURCHASE, SHARE 등을 카테고리로 사용
                            purchaseMethod = "ONLINE", // 기본값
                            thumbnailUrl = item.imageUrl ?: ""
                        )
                    }
                    adapter.updateList(sellerItems)
                } else {
                    adapter.updateList(emptyList())
                    Toast.makeText(this@MySaleItemsActivity, "판매 물품 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                adapter.updateList(emptyList())
                Toast.makeText(this@MySaleItemsActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }


} 
