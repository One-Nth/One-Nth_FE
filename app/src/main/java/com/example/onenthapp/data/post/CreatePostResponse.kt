package com.example.onenthapp.data.post

data class CreatePostResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Result?
) {
    data class Result(
        val postId: Long,
        val createdAt: String
    )
}

data class PostPayload(
    val title: String,
    val content: String,
    val address: String? = null,   // LIFE_TIP이면 null 가능
    val placeName: String? = null, // LIFE_TIP이면 null 가능
    val link: String? = null,      // DISCOUNT/RESTAURANT이면 null 가능
    val regionId: Long? = null,    // DISCOUNT/RESTAURANT에서 메인 지역 ID
    val tags: List<String> = emptyList()
)

