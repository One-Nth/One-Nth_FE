package com.example.onenthapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.data.MyPostProductItem
import com.example.onenthapp.data.post.PostRepository
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch

class MyPostProductFragment : Fragment(R.layout.fragment_mypost_product) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyPostProductAdapter
    private val repo by lazy { PostRepository(RetrofitInstance.memberApi) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewMyPostProduct)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ✅ 이 화면은 '내가 쓴 상품'이므로 삭제 버튼 노출 + 즉시 삭제 콜백
        adapter = MyPostProductAdapter(
            items = emptyList(),
            showDelete = true
        ) { item, position ->
            deleteNow(item, position)
        }
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
            runCatching {
                RetrofitInstance.memberApi.getMyItems(
                    bearerToken = "Bearer $token",
                    page = page,
                    size = size
                ).result?.items.orEmpty()
            }.onSuccess { items ->
                adapter.submitList(items)
            }.onFailure { e ->
                e.printStackTrace()
                Toast.makeText(requireContext(), "불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 확인 없이 즉시 삭제: UI 먼저 제거 → API 호출 → 실패 시 롤백 */
    private fun deleteNow(item: MyPostProductItem, position: Int) {
        // 현재 리스트 스냅샷(롤백용) 확보
        val before = adapter.currentItems().toMutableList()

        // 옵티미스틱 제거
        adapter.removeAt(position)

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { repo.deleteMyItem(item).getOrThrow() }
                .onSuccess {
                    Toast.makeText(requireContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .onFailure { e ->
                    // 실패 시 롤백
                    adapter.submitList(before)
                    Toast.makeText(requireContext(), "삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

