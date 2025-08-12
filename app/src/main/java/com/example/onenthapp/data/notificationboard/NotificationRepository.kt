package com.example.onenthapp.data.notificationboard

class NotificationboardRepository(
    private val api: NotificationboardApi
) {
    // 1. 게시글 댓글 등록
    suspend fun addCommentToPost(postId: Int, request: AddCommentToPostRequest) =
        api.addCommentToPost(postId, request)

    // 2. 게시글 댓글 조회
    suspend fun getPostComments(postId: Int) =
        api.getPostComments(postId)

    // 3. 게시글 댓글 삭제
    suspend fun deleteCommentFromPost(postId: Int, commentId: Int) =
        api.deleteCommentFromPost(postId, commentId)

    // 4. 스크랩 등록
    suspend fun scrapPost(postId: Int) =
        api.scrapPost(postId)

    // 5. 스크랩 삭제
    suspend fun unscrapPost(postId: Int) =
        api.unscrapPost(postId)

    // 6. 공감 등록
    suspend fun likePost(postId: Int) =
        api.likepost(postId)

    // 7. 공감 삭제
    suspend fun unlikePost(postId: Int) =
        api.unlikepost(postId)
}
