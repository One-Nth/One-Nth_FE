package com.example.onenthapp.data.chat

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MessageApi {

    // 채팅방 메세지 조회
    @GET("chats/{chatRoomId}/messages")
    suspend fun getChatMessages(
        @Path("chatRoomId") chatRoomId: Long
    ): Response<ChatResponse>

    // 채팅방 이름 조회 (생성 포함)
    @POST("chats/rooms")
    suspend fun createChatRoom(
        @Body request: ChatNameRequest
    ): Response<ChatNameResponse>

    // 채팅방 목록 조회
    @GET("chats/rooms")
    suspend fun getChatListMessages(
        @Path("chatRoomType") chatRoomType: String
    ): Response<ChatListResponse>

    // 채팅방 나가기
    @DELETE("chats/{chatRoomId}/leave")
    suspend fun leaveChatRoom(
        @Path("chatRoomId") chatRoomId: Long
    ): Response<LeaveChatResponse>
}