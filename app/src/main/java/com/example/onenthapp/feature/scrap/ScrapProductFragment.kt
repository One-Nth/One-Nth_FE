package com.example.onenthapp.feature.scrap

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.R
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.ScrapProductAdapter
import com.example.onenthapp.data.MyPostProductItem

import com.example.onenthapp.util.TokenManager
import com.example.onenthapp.ScrapActivity
import com.example.onenthapp.MainActivity

import kotlinx.coroutines.launch

class ScrapProductFragment : Fragment(R.layout.fragment_mypost_product) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScrapProductAdapter

    // 검색 상태
    private var fullList: List<MyPostProductItem> = emptyList()
    private var currentQuery: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewMyPostProduct)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ScrapProductAdapter(
            items = emptyList(),
            onItemClick = { item ->
                navigateToItemDetail(item)
            }
        )
        recyclerView.adapter = adapter

        // 검색어 수신(상품명만)
        parentFragmentManager.setFragmentResultListener(
            ScrapActivity.SEARCH_KEY, viewLifecycleOwner
        ) { _, bundle ->
            currentQuery = bundle.getString(ScrapActivity.SEARCH_BUNDLE_KEY).orEmpty()
            adapter.submitList(applyQuery(fullList, currentQuery))
        }

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
                val resp = com.example.onenthapp.RetrofitInstance.memberApi.getScrappedItems(
                    bearerToken = "Bearer $token",
                    page = page,
                    size = size
                )
                fullList = resp.result?.items.orEmpty()
                adapter.submitList(applyQuery(fullList, currentQuery))
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "스크랩 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 상품명만 필터 (필드명 프로젝트에 맞춰 조정)
    private fun applyQuery(src: List<MyPostProductItem>, q: String): List<MyPostProductItem> {
        if (q.isBlank()) return src
        val needle = q.trim().lowercase()

        return src.filter { item ->
            // 여기에 있는 후보 중 실제 존재하는 필드만 남겨도 됩니다.
            val candidates = listOfNotNull(
                item.productName,
            )
            candidates.any { it.contains(needle, ignoreCase = true) }
        }
    }

    /** 상품 클릭 시 상품 상세 조회로 이동 */
    private fun navigateToItemDetail(item: MyPostProductItem) {
        // ScrapActivity의 NavHost를 사용하여 상품 상세 표시
        val scrapActivity = requireActivity() as? ScrapActivity
        scrapActivity?.let { activity ->
            when (item.itemType) {
                "같이 사요" -> {
                    // 같이사요 상품 상세 조회
                    activity.showProductDetail(
                        productId = item.itemId,
                        isShare = false,
                        initialScraped = true
                    )
                }
                "함께 나눠요" -> {
                    // 함께나눠요 상품 상세 조회
                    activity.showProductDetail(
                        productId = item.itemId,
                        isShare = true,
                        initialScraped = true
                    )
                }
                else -> {
                    Toast.makeText(requireContext(), "알 수 없는 상품 타입입니다: ${item.itemType}", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }
    }
}
