package com.example.onenthapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyPostProductFragment : Fragment(R.layout.fragment_mypost_product) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewMyPostProduct)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 더미 데이터 (나중에 API 연결 시 교체)
        val products = listOf(
            mapOf(
                "tag" to "같이 사요",
                "name" to "상품 A",
                "info" to "가격 10,000원 / 2개 / 원래 12,000원",
                "views" to "조회수 24",
                "time" to "3분 전"
            ),
            mapOf(
                "tag" to "함께 나눠요",
                "name" to "상품 B",
                "info" to "가격 5,000원 / 1개 / 원래 7,000원",
                "views" to "조회수 12",
                "time" to "10분 전"
            )
        )

        recyclerView.adapter = ScrapProductAdapter(products) // 상품 카드용 Adapter
    }
}
