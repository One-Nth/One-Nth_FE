package com.example.onenthapp.chat

import android.util.Log
import com.example.onenthapp.data.chat.ChatMessage
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

class ChatWebSocketClient(
    private val chatRoomId: Int,
    private val memberId: Int,
    private val onMessageReceived: (ChatMessage) -> Unit
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    fun connect() {
        val request = Request.Builder()
            .url("ws://43.201.21.163:8080/ws/chat/$chatRoomId") // 실제 WebSocket URL로 교체
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("WebSocket", "Received: $text")
                try {
                    val json = JSONObject(text)
                    val message = ChatMessage(
                        senderMemberId = json.getInt("senderMemberId"),
                        content = json.getString("content"),
                        messageTime = json.getString("messageTime")
                    )
                    onMessageReceived(message)
                } catch (e: Exception) {
                    Log.e("WebSocket", "Parsing error: ${e.message}")
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error: ${t.message}")
            }
        })
    }

    fun sendMessage(message: ChatMessage) {
        val json = JSONObject().apply {
            put("chatRoomId", chatRoomId)
            put("senderMemberId", message.senderMemberId)
            put("content", message.content)
        }
        webSocket?.send(json.toString())
    }

    fun close() {
        webSocket?.close(1000, "User Left")
        webSocket = null
    }
}
