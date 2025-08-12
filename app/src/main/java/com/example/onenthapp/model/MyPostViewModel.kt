package com.example.onenthapp.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onenthapp.data.post.MyPostItem
import com.example.onenthapp.data.post.PostRepository
import kotlinx.coroutines.launch

class MyPostsViewModel(
    private val repo: PostRepository,
    private val postTypeFilter: String? = null
) : ViewModel() {

    data class UiState(
        val items: MutableList<MyPostItem> = mutableListOf(),
        val loading: Boolean = false,
        val error: String? = null,
        val currentPage: Int = 1,
        val isLast: Boolean = false
    )

    private val _state = MutableLiveData(UiState())
    val state: LiveData<UiState> = _state

    private fun List<MyPostItem>.applyFilter(): List<MyPostItem> {
        val f = postTypeFilter ?: return this
        // 서버 값만 비교: LIFE_TIP / DISCOUNT / RESTAURANT
        return filter { it.postType.equals(f, ignoreCase = true) }
    }

    fun loadFirst(pageSize: Int = 10) {          // ← token 제거
        _state.value = UiState(loading = true)
        viewModelScope.launch {
            repo.getMyPosts(page = 1, size = pageSize)
                .onSuccess { page ->
                    _state.value = UiState(
                        items = page.postList.applyFilter().toMutableList(),
                        loading = false,
                        currentPage = page.currentPage,
                        isLast = page.isLast
                    )
                }
                .onFailure { e ->
                    _state.value = UiState(loading = false, error = e.message ?: "알 수 없는 오류")
                }
        }
    }

    fun loadNext(pageSize: Int = 10) {           // ← token 제거
        val cur = _state.value ?: return
        if (cur.loading || cur.isLast) return

        _state.value = cur.copy(loading = true)
        viewModelScope.launch {
            val nextPage = cur.currentPage + 1
            repo.getMyPosts(page = nextPage, size = pageSize)
                .onSuccess { page ->
                    val merged = (cur.items + page.postList.applyFilter()).toMutableList()
                    _state.value = cur.copy(
                        items = merged,
                        loading = false,
                        currentPage = page.currentPage,
                        isLast = page.isLast,
                        error = null
                    )
                }
                .onFailure { e ->
                    _state.value = cur.copy(loading = false, error = e.message ?: "알 수 없는 오류")
                }
        }
    }
}

