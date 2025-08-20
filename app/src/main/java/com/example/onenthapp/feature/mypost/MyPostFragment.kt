package com.example.onenthapp.feature.mypost

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.EditPostActivity
import com.example.onenthapp.R
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.data.post.MyPostItem
import com.example.onenthapp.data.post.PostRepository
import com.example.onenthapp.model.MyPostsViewModel

class MyPostFragment : Fragment(R.layout.fragment_mypost) {

    private lateinit var vm: MyPostsViewModel
    private lateinit var adapter: MyPostAdapter

    private var fullList: List<MyPostItem> = emptyList()
    private var currentQuery: String = ""

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) vm.loadFirst()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.recyclerViewMyPost)
        rv.layoutManager = LinearLayoutManager(requireContext())

        adapter = MyPostAdapter(
            onItemClick = { /* 상세 진입 필요시 */ }
        ).apply {
            setShowExtraIcon(true)
            setOnExtraClick { item ->
                val i = Intent(requireContext(), EditPostActivity::class.java).apply {
                    putExtra("postId", item.postId)
                    putExtra("postType", item.postType)
                }
                editLauncher.launch(i)
            }
        }
        rv.adapter = adapter

        // VM
        val api = RetrofitInstance.memberApi
        val repo = PostRepository(api)
        val filter = arguments?.getString("filter")
        vm = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MyPostsViewModel(repo, postTypeFilter = filter) as T
            }
        })[MyPostsViewModel::class.java]

        // 데이터 observe → 항상 현재 검색어로 필터 후 표시
        vm.state.observe(viewLifecycleOwner) { s ->
            fullList = s.items
            adapter.submitList(applyQuery(fullList, currentQuery))
            s.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }

        // ✅ Activity에서 뿌린 검색어 수신
        parentFragmentManager.setFragmentResultListener(
            MyPostActivity.SEARCH_KEY, viewLifecycleOwner
        ) { _, bundle ->
            currentQuery = bundle.getString(MyPostActivity.SEARCH_BUNDLE_KEY).orEmpty()
            adapter.submitList(applyQuery(fullList, currentQuery))
        }

        // 첫 로드 + 무한 스크롤
        vm.loadFirst()
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (lm.findLastVisibleItemPosition() >= adapter.itemCount - 3) vm.loadNext()
            }
        })
    }

    private fun applyQuery(src: List<MyPostItem>, q: String): List<MyPostItem> {
        if (q.isBlank()) return src
        val lower = q.lowercase()
        return src.filter { item ->
            listOfNotNull(
                item.postTitle,
                item.content,
                item.placeName,
                item.regionName
            ).any { it.lowercase().contains(lower) }
        }
    }

    companion object {
        fun newInstance(filter: String?): MyPostFragment =
            MyPostFragment().apply { arguments = bundleOf("filter" to filter) }
    }
}

