package com.example.onenthapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyPostNFragment : Fragment(R.layout.fragment_mypost_n) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewMyPost)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 예시 데이터 (나중에 API 연동 시 교체)
        val posts = listOf(
            mapOf(
                "category" to "생활 정보",
                "title" to "내가 쓴 첫 번째 글",
                "content" to "아메리카노 2000원 행사 소식!",
                "commentCount" to "3",
                "likeCount" to "2",
                "views" to "50",
                "time" to "5분 전"
            ),
            mapOf(
                "category" to "우리 동네 맛집/카페",
                "title" to "OOO카페 방문 후기",
                "content" to "카페 분위기가 정말 좋았어요.",
                "commentCount" to "1",
                "likeCount" to "0",
                "views" to "20",
                "time" to "10분 전"
            )
        )

        recyclerView.adapter = ScrapPostAdapter(posts) // 기존 게시글 Adapter 재사용
    }
}
