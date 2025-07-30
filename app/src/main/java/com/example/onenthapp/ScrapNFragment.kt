package com.example.onenthapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScrapNFragment : Fragment(R.layout.fragment_scrap_n) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewScrapPost)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Map 형태로 데이터 전달
        val posts = listOf(
            mapOf(
                "category" to "생활 정보",
                "title" to "OOO카페",
                "content" to "아메리카노 2000원 행사해요~",
                "commentCount" to "2",
                "likeCount" to "1",
                "views" to "24",
                "time" to "3분전"
            ),
            mapOf(
                "category" to "생활 꿀팁",
                "title" to "OOO카페",
                "content" to "아메리카노 2000원 행사해요~",
                "commentCount" to "5",
                "likeCount" to "2",
                "views" to "12",
                "time" to "10분전"
            )
        )

        recyclerView.adapter = ScrapPostAdapter(posts)
    }
}