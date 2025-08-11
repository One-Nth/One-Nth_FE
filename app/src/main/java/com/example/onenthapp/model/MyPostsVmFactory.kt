package com.example.onenthapp.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.onenthapp.data.post.PostRepository

class MyPostsVmFactory(
    private val repo: PostRepository,
    private val postType: String?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MyPostsViewModel(repo, postType) as T
    }
}
