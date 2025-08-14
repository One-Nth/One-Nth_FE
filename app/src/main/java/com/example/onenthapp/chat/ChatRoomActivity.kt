package com.example.onenthapp.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.R
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.data.chat.ChatMessage
import com.example.onenthapp.databinding.ActivityChatRoomBinding
import com.example.onenthapp.databinding.ChatTopToolbarBinding
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var webSocketClient: ChatWebSocketClient
    private lateinit var toolbarBinding: ChatTopToolbarBinding


    private val myMemberId: Int by lazy {
        TokenManager.getMemberId()?.toInt() ?: run {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            0  // 혹은 적절한 기본값(0 등)
        }
    }
    private var chatRoomId: Int = -1
    private var peerNickname: String = "익명"
    private var roomName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatRoomId = intent.getIntExtra("chatRoomId", -1)
        peerNickname = intent.getStringExtra("peerNickname") ?: "익명"
        roomName = intent.getStringExtra("roomName") ?: ""
        toolbarBinding = ChatTopToolbarBinding.bind(binding.chatTopToolbar)

        if (chatRoomId == -1) {
            Toast.makeText(this, "채팅방 정보가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        webSocketClient = ChatWebSocketClient(
            roomName = roomName,
            memberId = myMemberId
        ) { message ->
            runOnUiThread {
//                chatAdapter.addMessage(message)
                binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
            }
        }
        webSocketClient.connect()

        toolbarBinding.title.text = peerNickname
        toolbarBinding.btnLeft.setOnClickListener { finish() }
        toolbarBinding.btnMenu.setOnClickListener { showChatMenu() }

        setupToolbar(peerNickname)
        setupRecyclerView()
        loadMessages()
        setupSendButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
    }

    private fun setupToolbar(peerNickname: String) {
        toolbarBinding.title.text = peerNickname
        toolbarBinding.btnLeft.setOnClickListener { finish() }
        toolbarBinding.btnMenu.setOnClickListener { showChatMenu() }
    }


    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(myMemberId)
        binding.chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ChatRoomActivity)
        }
    }

    private fun showChatMenu() {
        binding.inputLayout.visibility = View.GONE
        binding.inputLayoutGone.visibility = View.VISIBLE
        binding.chatMenuContainer.visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .replace(R.id.chatMenuContainer, ChatMenuFragment.newInstance(chatRoomId, roomName))
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

