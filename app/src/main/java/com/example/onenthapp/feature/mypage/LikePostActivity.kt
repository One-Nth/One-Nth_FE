package com.example.onenthapp.feature.mypage

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.R
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.data.post.MyPostItem
import com.example.onenthapp.data.post.PostRepository
import com.example.onenthapp.feature.board.LifeTipsDetailActivity
import com.example.onenthapp.feature.mypost.MyPostAdapter
import com.example.onenthapp.model.MyLikesViewModel

class LikePostActivity : AppCompatActivity() {

    private lateinit var vm: MyLikesViewModel
    private lateinit var adapter: MyPostAdapter

    // 검색용 전체 리스트 보관
    private var fullList: List<MyPostItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like_post) //

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.recyclerViewLikePost)
        rv.layoutManager = LinearLayoutManager(this)
// ✅ 클릭하면 상세로 이동
        adapter = MyPostAdapter { item ->
            val intent = Intent(this, LifeTipsDetailActivity::class.java).apply {
                putExtra("postId", item.postId)   // Long
                putExtra("liked", true)           // 공감 목록에서 진입 → 초기 하트 on
            }
            startActivity(intent)
        }
        rv.adapter = adapter


        // VM
        val api = RetrofitInstance.memberApi
        val repo = PostRepository(api)
        vm = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // ✅ 필터 없음 → 공감한 모든 게시글
                return MyLikesViewModel(repo, postTypeFilter = null) as T
            }
        })[MyLikesViewModel::class.java]


        // Observe
        vm.state.observe(this) { s ->
            fullList = s.items
            adapter.submitList(applyQuery(fullList, currentQuery))
            s.error?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        // 첫 로드
        vm.loadFirst()

        // 무한 스크롤
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (lm.findLastVisibleItemPosition() >= adapter.itemCount - 3) vm.loadNext()
            }
        })

        // 검색
        val searchEt = findViewById<EditText>(R.id.search_bar_et)
        searchEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                adapter.submitList(applyQuery(fullList, searchEt.text.toString()))
                true
            } else false
        }
        searchEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentQuery = s?.toString().orEmpty()
                adapter.submitList(applyQuery(fullList, currentQuery))
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

    }

    private var currentQuery: String = ""

    private fun applyQuery(src: List<MyPostItem>, q: String): List<MyPostItem> {
        if (q.isBlank()) return src
        val lower = q.lowercase()
        return src.filter { item ->
            listOfNotNull(
                item.postTitle,
                item.placeName,
                item.regionName
            ).any { it.lowercase().contains(lower) }
        }
    }
}
