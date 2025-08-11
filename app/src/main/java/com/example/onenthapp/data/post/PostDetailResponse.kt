package com.example.onenthapp.data.post

data class PostDetailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Detail?
) {
    data class Detail(
        val postId: String,              // 서버가 문자열로 내려줌
        val nickname: String?,
        val profileImageUrl: String?,
        val regionName: String?,         // LIFE_TIP이면 null
        val title: String,
        val content: String,
        val imageUrls: List<String>?,    // 0~N
        val commentCount: Int,
        val likeCount: Int,
        val scrapStatus: Boolean,
        val viewCount: Int,
        val createdAt: String
    )
}
