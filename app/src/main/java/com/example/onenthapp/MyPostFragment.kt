package com.example.onenthapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.data.post.PostRepository
import com.example.onenthapp.model.MyPostsViewModel
import com.example.onenthapp.util.TokenManager

class MyPostFragment : Fragment(R.layout.fragment_mypost) {

    private lateinit var vm: MyPostsViewModel
    private lateinit var adapter: MyPostAdapter

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            vm.loadFirst() // 수정/삭제 후 목록 새로고침
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.recyclerViewMyPost)
        rv.layoutManager = LinearLayoutManager(requireContext())

        adapter = MyPostAdapter(
            onItemClick = { /* ... */ }
        ).apply {
            setShowExtraIcon(true)// "내 글" 화면이므로 ON
            setOnExtraClick { item ->
                val i = Intent(requireContext(), EditPostActivity::class.java).apply {
                    putExtra("postId", item.postId)
                    putExtra("postType", item.postType)
                }
                editLauncher.launch(i) // 또는 startActivity(i)
            }
        }
        rv.adapter = adapter

        // VM 생성 (postTypeFilter는 필요 시 "TIP")
        val api = RetrofitInstance.memberApi
        val repo = PostRepository(api)
        val filter = arguments?.getString("filter")
        vm = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MyPostsViewModel(repo, postTypeFilter = filter) as T
            }
        })[MyPostsViewModel::class.java]

        vm.state.observe(viewLifecycleOwner) { s ->
            adapter.submitList(s.items.toList())
            s.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }

        // ── 여기 수정 ─────────────────────────────────────────────
        val token = TokenManager.getAccessToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        vm.loadFirst()              // ✅ token 인자 제거 (기본 pageSize=10)
        // vm.loadFirst(10)         // (원하면 pageSize 직접 지정)

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm = recyclerView.layoutManager as LinearLayoutManager
                val last = lm.findLastVisibleItemPosition()
                if (last >= adapter.itemCount - 3) {
                    vm.loadNext()   // ✅ token 인자 제거
                    // vm.loadNext(10)
                }
            }
        })
        // ─────────────────────────────────────────────────────────
    }
}

