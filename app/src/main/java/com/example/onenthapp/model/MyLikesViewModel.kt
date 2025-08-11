package com.example.onenthapp.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onenthapp.data.post.MyPostItem
import com.example.onenthapp.data.post.PostRepository
import kotlinx.coroutines.launch

class MyLikesViewModel(
    private val repo: PostRepository,
    private val postTypeFilter: String? = "TIP" // 공감은 TIP 고정
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

    private fun List<MyPostItem>.applyFilter(): List<MyPostItem> =
        postTypeFilter?.let { f -> filter { it.postType.equals(f, true) } } ?: this

    fun loadFirst(pageSize: Int = 10) {
        _state.value = UiState(loading = true)
        viewModelScope.launch {
            repo.getMyLikedPosts(1, pageSize)
                .onSuccess { p ->
                    _state.value = UiState(
                        items = p.postList.applyFilter().toMutableList(),
                        loading = false,
                        currentPage = p.currentPage,
                        isLast = p.isLast
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
            repo.getMyLikedPosts(cur.currentPage + 1, pageSize)
                .onSuccess { p ->
                    _state.value = cur.copy(
                        items = (cur.items + p.postList.applyFilter()).toMutableList(),
                        loading = false,
                        currentPage = p.currentPage,
                        isLast = p.isLast,
                        error = null
                    )
                }
                .onFailure { e ->
                    _state.value = cur.copy(loading = false, error = e.message ?: "서버 오류")
                }
        }
    }
}
