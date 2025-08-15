package com.example.onenthapp.data.chat

import com.example.onenthapp.data.CommonResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MessageApi {

    // 채팅방 메세지 조회
    @GET("chats/{chatRoomId}/messages")
    suspend fun getChatMessages(
        @Path("chatRoomId") chatRoomId: Int
    ): Response<ChatResponse>

    // 채팅방 이름 조회 (생성 포함)
    @POST("chats/rooms")
    suspend fun createChatRoom(
        @Query("targetMemberId") targetMemberId: Int,
        @Query("chatRoomType") chatRoomType: String
    ): Response<ChatNameResponse>


    // 채팅방 목록 조회
    @GET("chats/rooms")
    suspend fun getChatListMessages(
        @Query("chatRoomType") chatRoomType: String
    ): Response<ChatListResponse>

    // 채팅방 나가기
    @DELETE("chats/{chatRoomId}/leave")
    suspend fun leaveChatRoom(
        @Path("chatRoomId") chatRoomId: Int
    ): Response<LeaveChatResponse>

    // id로 닉네임 조회
    @GET("members/{memberId}/profile")
    suspend fun getMemberNickname(
        @Path("memberId") memberId: Int
    ): Response<MemberNickName>

    // 사용자 차단하기
    @POST("members/block/{targetMemberId}")
    suspend fun blockMember(
        @Path("targetMemberId") targetMemberId: Int
    ): Response<CommonResponse<String>>
}