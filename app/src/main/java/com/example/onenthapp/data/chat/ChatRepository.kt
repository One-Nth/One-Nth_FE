package com.example.onenthapp.data.chat

import com.example.onenthapp.RetrofitInstance
import retrofit2.Response

class ChatRepository {
    private val api = RetrofitInstance.messageApi

    // 채팅방 메세지 조회
    suspend fun getMessages(chatRoomId: Int) =
        api.getChatMessages(chatRoomId)

    // 채팅방 이름 조회
    suspend fun getChatRoomName(targetMemberId: Int, chatRoomType: String): Response<ChatNameResponse> =
        api.createChatRoom(ChatNameRequest(targetMemberId, chatRoomType))

    // 채팅 목록 조회
    suspend fun getChatRoomList(chatRoomType: String): Response<ChatListResponse> =
        api.getChatListMessages(chatRoomType)

    // 채팅방 나가기
    suspend fun leaveChatRoom(chatRoomId: Int): Response<LeaveChatResponse> =
        api.leaveChatRoom(chatRoomId)
}
