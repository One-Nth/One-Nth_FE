package com.example.onenthapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch

class ScrapProductFragment : Fragment(R.layout.fragment_mypost_product) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScrapProductAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerViewMyPostProduct)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ScrapProductAdapter(emptyList())
        recyclerView.adapter = adapter

        loadPage(page = 1, size = 10)
    }

    private fun loadPage(page: Int, size: Int) {
        val token = TokenManager.getAccessToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.memberApi.getScrappedItems(
                    bearerToken = "Bearer $token",
                    page = page,
                    size = size
                )
                val items = resp.result?.items.orEmpty()
                adapter.submitList(items)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "스크랩 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}