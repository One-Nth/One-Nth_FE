package com.example.onenthapp.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onenthapp.data.post.MyPostItem
import com.example.onenthapp.data.post.PostRepository
import kotlinx.coroutines.launch

class MyScrapsViewModel(
    private val repo: PostRepository,
    private val postTypeFilter: String? = "TIP"
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
        return filter { it.postType.equals(f, ignoreCase = true) }
    }

    fun loadFirst(pageSize: Int = 10) {
        _state.value = UiState(loading = true)
        viewModelScope.launch {
            repo.getMyScrapPosts(page = 1, size = pageSize)
                .onSuccess { page ->
                    _state.value = UiState(
                        items = page.postList.applyFilter().toMutableList(),
                        loading = false,
                        currentPage = page.currentPage,
                        isLast = page.isLast
                    )
                }
                .onFailure { e ->
                    _state.value = UiState(loading = false, error = e.message ?: "서버 오류")
                }
        }
    }

    fun loadNext(pageSize: Int = 10) {
        val cur = _state.value ?: return
        if (cur.loading || cur.isLast) return

        _state.value = cur.copy(loading = true)
        viewModelScope.launch {
            val nextPage = cur.currentPage + 1
            repo.getMyScrapPosts(page = nextPage, size = pageSize)
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
                    _state.value = cur.copy(loading = false, error = e.message ?: "서버 오류")
                }
        }
    }
}
