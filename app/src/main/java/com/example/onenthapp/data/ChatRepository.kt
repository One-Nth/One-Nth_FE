package com.example.onenthapp.data

import com.example.onenthapp.RetrofitInstance
import retrofit2.Response

class ChatRepository {
    private val api = RetrofitInstance.messageApi

    /* 채팅 내역 조회 (GET) */
    suspend fun getMessages(chatRoomId: Long) =
        api.getChatMessages(chatRoomId)

    suspend fun getChatRoomName(targetMemberId: Int, chatRoomType: String): Response<ChatNameResponse> =
        api.createChatRoom(ChatNameRequest(targetMemberId, chatRoomType))

}
