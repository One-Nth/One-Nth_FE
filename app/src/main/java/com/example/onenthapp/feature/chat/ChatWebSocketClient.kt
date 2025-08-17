package com.example.onenthapp.feature.chat

import android.util.Log
import com.example.onenthapp.data.chat.ChatMessage
import com.example.onenthapp.util.TokenManager
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

class ChatWebSocketClient(
    private val roomName: String,
    private val memberId: Int,
    private val onMessageReceived: (ChatMessage) -> Unit
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val TAG = "ChatWebSocket"

    fun connect() {
        val token = TokenManager.getAccessToken()
        val request = Request.Builder()
            .url("ws://43.201.21.163:8080/ws-chat") // ✅ 기본 WebSocket URL
            .addHeader("Authorization", "Bearer $token") // ✅ JWT 포함
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket Connected")

                // ✅ STOMP CONNECT
                val connectFrame = "CONNECT\naccept-version:1.1,1.2\nAuthorization:Bearer $token\n\n\u0000"
                ws.send(connectFrame)

                // ✅ 구독 (SUBSCRIBE)
                val subscribeFrame = "SUBSCRIBE\nid:sub-0\ndestination:/sub/chat-rooms/$roomName\n\n\u0000"
                ws.send(subscribeFrame)
            }


                override fun onMessage(ws: WebSocket, text: String) {
                    Log.d(TAG, "Raw Message Received: $text")

                    if (text.startsWith("MESSAGE")) {
                        try {
                            val body = text.substringAfter("\n\n").trim('\u0000')

                            // ✅ body 자체를 ChatMessage.content에 넘긴다!
                            val message = ChatMessage(
                                senderMemberId = memberId, // 또는 json.getInt("sendMemberId")로 유지해도 OK
                                content = body,
                                messageTime = "" // 또는 json.optString("messageTime")
                            )
                            onMessageReceived(message)

                        } catch (e: Exception) {
                            Log.e(TAG, "Message Parsing Failed: ${e.message}")
                        }
                    }
                }


                override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket Failure: ${t.message}")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket Closed: $code / $reason")
            }
        })
    }

    fun sendMessage(message: ChatMessage) {
        val json = JSONObject().apply {
            put("sendMemberId", memberId)
            put("content", message.content)
        }

        // ✅ STOMP SEND Frame
        val sendFrame = buildString {
            append("SEND\n")
            append("destination:/pub/chat-rooms/$roomName\n")
            append("content-type:application/json\n\n")
            append(json.toString())
            append("\u0000")
        }

        webSocket?.send(sendFrame)
    }

    fun close() {
        webSocket?.close(1000, "Client Closed")
        webSocket = null
    }
}
