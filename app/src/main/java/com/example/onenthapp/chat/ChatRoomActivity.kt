package com.example.onenthapp.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.R
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.data.chat.ChatMessage
import com.example.onenthapp.databinding.ActivityChatRoomBinding
import com.example.onenthapp.databinding.ChatTopToolbarBinding
import kotlinx.coroutines.launch

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var webSocketClient: ChatWebSocketClient

    private val chatRoomId = 1 // 실제 프로젝트에서는 intent로 받아올 것
    private val myMemberId = 1 // 본인 ID (로그인 정보 기준)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webSocketClient = ChatWebSocketClient(
            chatRoomId = chatRoomId,
            memberId = myMemberId
        ) { message ->
            runOnUiThread {
                chatAdapter.addMessage(message)
                binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
            }
        }
        webSocketClient.connect()


        setupRecyclerView()
        setupToolbar()
        loadMessages()
        setupSendButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
    }


    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(myMemberId)
        binding.chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ChatRoomActivity)
        }
    }
    private fun setupToolbar() {
        val toolbarBinding = ChatTopToolbarBinding.bind(binding.chatTopToolbar.getChildAt(0))
        toolbarBinding.btnLeft.setOnClickListener { finish() }

        toolbarBinding.btnMenu.setOnClickListener {
            showChatMenu() // <-- 여기로 단순화
        }
    }
    private fun showChatMenu() {
        // 1. 입력창 숨기기
        binding.inputLayout.visibility = View.GONE
        binding.inputLayoutGone.visibility = View.VISIBLE

        // 2. 메뉴 Fragment 보이기
        binding.chatMenuContainer.visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .replace(R.id.chatMenuContainer, ChatMenuFragment())
            .commit()
    }



    private fun loadMessages() {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.messageApi.getChatMessages(chatRoomId)
                if (response.isSuccessful) {
                    response.body()?.result?.let { messages ->
                        chatAdapter.setMessages(messages)
                        binding.chatRecyclerView.scrollToPosition(messages.size - 1)
                    }
                } else {
                    Log.e("ChatRoom", "메시지 조회 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ChatRoom", "에러: ${e.message}")
            }
        }
    }

    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val content = binding.messageInput.text.toString().trim()
            if (content.isNotEmpty()) {
                val newMessage = ChatMessage(
                    senderMemberId = myMemberId,
                    content = content,
                    messageTime = "" // 서버에서 시간 처리할 수도 있음
                )

                // 로컬 UI에 먼저 보여주기
                chatAdapter.addMessage(newMessage)
                binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                binding.messageInput.text.clear()

                // 서버에 WebSocket으로 전송
                webSocketClient.sendMessage(newMessage)
            }
        }
    }

}

