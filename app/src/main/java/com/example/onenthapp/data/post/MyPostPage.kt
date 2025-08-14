package com.example.onenthapp.data.post

data class MyPostsPage(
    val listSize: Int,
    val totalPage: Int,
    val totalElements: Long,
    val currentPage: Int,
    val isFirst: Boolean,
    val isLast: Boolean,
    val postList: List<MyPostItem>
)

// 게시글 미리보기 아이템
data class MyPostItem(
    val postId: Long,
    val postType: String,
    val postTitle: String,
    val content: String,
    val placeName: String?,    // 서버가 null 줄 수도 있어서 안전하게
    val latitude: Double?,
    val longitude: Double?,
    val regionName: String?,   // ⚠️ NPE 방지 위해 nullable
    val commentCount: Int,
    val likeCount: Int,
    val viewCount: Int,
    val createdTime: String    // 예: "2025-08-10T21:07:28.303820"
)
