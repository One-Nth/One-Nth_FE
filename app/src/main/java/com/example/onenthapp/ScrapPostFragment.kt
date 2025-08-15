// ScrapPostFragment.kt
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
import com.example.onenthapp.data.post.MyPostItem
import com.example.onenthapp.data.post.PostRepository
import com.example.onenthapp.databinding.FragmentScrapNtipBinding
import com.example.onenthapp.model.MyScrapsViewModel
import com.example.onenthapp.util.TokenManager

class ScrapPostFragment : Fragment(R.layout.fragment_scrap_ntip) {

    private var _binding: FragmentScrapNtipBinding? = null
    private val binding get() = _binding!!

    private lateinit var vm: MyScrapsViewModel
    private lateinit var adapter: MyPostAdapter

    private var fullList: List<MyPostItem> = emptyList()
    private var currentQuery: String = ""

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK &&
            res.data?.getBooleanExtra("needRefresh", false) == true
        ) {
            vm.loadFirst()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentScrapNtipBinding.bind(view)

        // ✅ 여기서 더 이상 findViewById 안 씀
        binding.recyclerViewScrapProduct.layoutManager = LinearLayoutManager(requireContext())

        adapter = MyPostAdapter { item ->
            val intent = Intent(requireContext(), LifeTipsDetailActivity::class.java).apply {
                putExtra("postId", item.postId)
                putExtra("scrapped", true)
            }
            detailLauncher.launch(intent)
        }
        binding.recyclerViewScrapProduct.adapter = adapter

        // 검색어 수신
        parentFragmentManager.setFragmentResultListener(
            ScrapActivity.SEARCH_KEY, viewLifecycleOwner
        ) { _, bundle ->
            currentQuery = bundle.getString(ScrapActivity.SEARCH_BUNDLE_KEY).orEmpty()
            adapter.submitList(applyQuery(fullList, currentQuery))
        }

        // VM
        val api = RetrofitInstance.memberApi
        val repo = PostRepository(api)
        vm = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MyScrapsViewModel(repo, postTypeFilter = null) as T
            }
        })[MyScrapsViewModel::class.java]

        vm.state.observe(viewLifecycleOwner) { s ->
            fullList = s.items.toList()
            adapter.submitList(applyQuery(fullList, currentQuery))
            s.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }

        val token = TokenManager.getAccessToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        vm.loadFirst()

        binding.recyclerViewScrapProduct.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm = rv.layoutManager as LinearLayoutManager
                if (lm.findLastVisibleItemPosition() >= adapter.itemCount - 3) {
                    vm.loadNext()
                }
            }
        })
    }

    private fun applyQuery(src: List<MyPostItem>, q: String): List<MyPostItem> {
        if (q.isBlank()) return src
        val needle = q.lowercase()
        return src.filter { item ->
            listOfNotNull(item.postTitle, item.placeName, item.regionName)
                .any { it.lowercase().contains(needle) }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
