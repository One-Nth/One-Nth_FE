package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.data.TipsDummyData
import com.example.onenthapp.databinding.ActivityTipsSearchBinding
import com.example.onenthapp.model.TipItem

class LifeTipsSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTipsSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipsSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val query = intent.getStringExtra("query") ?: ""
        val boardType = intent.getStringExtra("boardType") ?: "life_tip"
        binding.tvSearchKeyword.text = query

        // 게시판 타입에 따른 더미 데이터 로드
        val dummyResults: List<TipItem> = when (boardType) {
            "discount" -> TipsDummyData.getDiscountDummy()
            "cafe" -> TipsDummyData.getCafeDummy()
            "life_tip" -> TipsDummyData.getLifeTipDummy()
            else-> emptyList()
        }

        // 검색어 필터링
        val filteredResults = dummyResults.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
        }

        // 리사이클러뷰 적용 + 클릭 시 상세 화면 이동
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(this@LifeTipsSearchActivity)
            adapter = LifeTipsSearchAdapter(filteredResults) { clickedItem ->
                val intent = Intent(this@LifeTipsSearchActivity, LifeTipsDetailActivity::class.java).apply {
                    putExtra("title", clickedItem.title)
                    putExtra("content", clickedItem.content)
                    putExtra("timeAgo", clickedItem.timeAgo)
                    putExtra("commentCount", clickedItem.commentCount)
                    putExtra("likeCount", clickedItem.likeCount)
                    putExtra("viewCount", clickedItem.viewCount)
                }
                startActivity(intent)
            }
        }

        // 뒤로 가기 버튼 처리
        binding.ivBack.setOnClickListener {
            finish()
        }
    }
}