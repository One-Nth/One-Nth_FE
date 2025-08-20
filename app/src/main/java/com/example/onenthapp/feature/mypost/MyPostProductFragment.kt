package com.example.onenthapp.feature.mypost

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
import com.example.onenthapp.data.MyPostProductItem
import com.example.onenthapp.data.post.PostRepository
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.util.TokenManager
import com.example.onenthapp.MyPostActivity

import kotlinx.coroutines.launch
//import kotlinx.coroutines.runCatching

class MyPostProductFragment : Fragment(R.layout.fragment_mypost_product) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyPostProductAdapter
    private val repo by lazy { PostRepository(RetrofitInstance.memberApi) }

    // ✅ 검색 상태
    private var fullList: List<MyPostProductItem> = emptyList()
    private var currentQuery: String = ""

    private val SEARCH_KEY = MyPostActivity.SEARCH_KEY
    private val SEARCH_VALUE_KEY = MyPostActivity.SEARCH_BUNDLE_KEY

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewMyPostProduct)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ✅ 내 상품 화면: 삭제 버튼 노출 + 즉시 삭제
        adapter = MyPostProductAdapter(
            items = emptyList(),
            showDelete = true,
            onDeleteClick = { item, position ->
                deleteNow(item, position)
            },
            onItemClick = { item ->
                navigateToItemDetail(item)
            }
        )
        recyclerView.adapter = adapter

        // ✅ 검색어 브로드캐스트 수신 → 상품명 기준 필터
        parentFragmentManager.setFragmentResultListener(SEARCH_KEY, viewLifecycleOwner) { _, bundle ->
            currentQuery = bundle.getString(SEARCH_VALUE_KEY).orEmpty()
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
            runCatching {
                RetrofitInstance.memberApi.getMyItems(
                    bearerToken = "Bearer $token",
                    page = page,
                    size = size
                ).result?.items.orEmpty()
            }.onSuccess { items ->
                // ✅ 전체 리스트 유지해두고, 항상 현재 검색어로 필터해서 노출
                fullList = items
                adapter.submitList(applyQuery(fullList, currentQuery))
            }.onFailure { e ->
                e.printStackTrace()
                Toast.makeText(requireContext(), "불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 확인 없이 즉시 삭제: UI 먼저 제거 → API → 실패 시 롤백 */
    private fun deleteNow(item: MyPostProductItem, position: Int) {
        val beforeAdapterItems = adapter.currentItems().toMutableList()
        val beforeFullList = fullList.toMutableList()

        // 화면/원본에서 낙관적 제거
        adapter.removeAt(position)
        fullList = fullList.filterNot { it.itemId == item.itemId }

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { repo.deleteMyItem(item).getOrThrow() }
                .onSuccess {
                    Toast.makeText(requireContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .onFailure { e ->
                    // 실패 시 롤백
                    adapter.submitList(beforeAdapterItems)
                    fullList = beforeFullList
                    Toast.makeText(requireContext(), "삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    /** ✅ ‘상품명’만 대상으로 필터 */
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
        // MyPostActivity의 NavHost를 사용하여 상품 상세 표시
        val myPostActivity = requireActivity() as? MyPostActivity
        myPostActivity?.let { activity ->
            when (item.itemType) {
                "같이 사요" -> {
                    // 같이사요 상품 상세 조회
                    activity.showProductDetail(
                        productId = item.itemId,
                        isShare = false,
                        initialScraped = false
                    )
                }
                "함께 나눠요" -> {
                    // 함께나눠요 상품 상세 조회
                    activity.showProductDetail(
                        productId = item.itemId,
                        isShare = true,
                        initialScraped = false
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
