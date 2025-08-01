package com.example.onenthapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScrapTipNFragment : Fragment(R.layout.fragment_scrap_ntip) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewScrapProduct)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Map 리스트 형태로 데이터 전달
        val products = listOf(
            mapOf(
                "tag" to "같이 사요",
                "name" to "상품 A",
                "info" to "가격 10,000원 / 2개 / 원래 12,000원",
                "views" to "조회수 24",
                "time" to "3분 전"
            ),
            mapOf(
                "tag" to "공동구매",
                "name" to "상품 B",
                "info" to "가격 5,000원 / 1개 / 원래 7,000원",
                "views" to "조회수 15",
                "time" to "10분 전"
            )
        )

        recyclerView.adapter = ScrapProductAdapter(products)
    }
}
