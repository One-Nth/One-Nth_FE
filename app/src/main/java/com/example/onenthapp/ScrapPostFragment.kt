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
import com.example.onenthapp.model.MyScrapsViewModel

class ScrapPostFragment : Fragment(R.layout.fragment_scrap_ntip) {

    private lateinit var vm: MyScrapsViewModel
    private lateinit var adapter: MyPostAdapter  // ← MyPostItem용 어댑터 재사용

    // 상세에서 취소 후 돌아오면 목록 새로고침
    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK &&
            res.data?.getBooleanExtra("needRefresh", false) == true) {
            vm.loadFirst()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.recyclerViewScrapProduct /* or proper id */)
        rv.layoutManager = LinearLayoutManager(requireContext())
        // ✅ 클릭하면 상세로 이동 + "scrapped=true" 전달
        adapter = MyPostAdapter { item ->
            val intent = Intent(requireContext(), LifeTipsDetailActivity::class.java).apply {
                putExtra("postId", item.postId)   // Long
                putExtra("scrapped", true)        // 스크랩 목록에서 진입
            }
            detailLauncher.launch(intent)
        }
        rv.adapter = adapter


        val api = RetrofitInstance.memberApi
        val repo = PostRepository(api)
        vm = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // ✅ 게시판 구분 없이 전체 null
                return MyScrapsViewModel(repo, postTypeFilter = null) as T
            }
        })[MyScrapsViewModel::class.java]

        vm.state.observe(viewLifecycleOwner) { s ->
            adapter.submitList(s.items.toList())
            // 필요하면 로딩/빈화면/에러 처리
            s.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }

        vm.loadFirst()

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (lm.findLastVisibleItemPosition() >= adapter.itemCount - 3) {
                    vm.loadNext()
                }
            }
        })
    }
}
