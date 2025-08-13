package com.example.onenthapp.data.notificationboard

import com.example.onenthapp.data.CommonResponse
import com.example.onenthapp.data.chat.LeaveChatResponse
import com.example.onenthapp.data.transaction.AvailableProductsResponse
import com.example.onenthapp.data.transaction.CancelTransactionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationboardApi {
    // 1. 게시글 댓글 등록
    @POST("post/{postId}/comment")
    suspend fun addCommentToPost(
        @Path("postId") postId: Int,
        @Body request: AddCommentToPostRequest
    ): Response<AddCommentToPostResponse>

    // 2. 게시글 댓글 조회
    @GET("post/{postId}/comment/list")
    suspend fun getPostComments(
        @Path("postId") postId: Int
    ): Response<GetPostCommentsResponse>

    // 3. 게시글 댓글 삭제
    @DELETE("post/{postId}/comment/{commentId}")
    suspend fun deleteCommentFromPost(
        @Path("postId") postId: Int,
        @Path("commentId") commentId: Int
    ): Response<CommonResponse<String>>

    // 4. 스크랩 등록
    @POST("post/{postId}/scrap")
    suspend fun scrapPost(
        @Path("postId") postId: Int,
    ): Response<ScrapPostResponse>

    // 5. 스크랩 삭제
    @DELETE("post/{postId}/scrap")
    suspend fun unscrapPost(
        @Path("postId") postId: Int,
    ): Response<ScrapPostResponse>

    // 6. 공감 등록
    @POST("post/{postId}/like")
    suspend fun likepost(
        @Path("postId") postId: Int,
    ): Response<ScrapPostResponse>

    // 7. 공감 삭제
    @DELETE("post/{postId}/like")
    suspend fun unlikepost(
        @Path("postId") postId: Int,
    ): Response<ScrapPostResponse>
}