package com.example.onenthapp.feature.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.R
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.chat.CancelDealActivity
import com.example.onenthapp.data.chat.ChatMessage
import com.example.onenthapp.databinding.ActivityChatRoomBinding
import com.example.onenthapp.databinding.ChatTopToolbarBinding
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch
import org.json.JSONObject

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data?.getBooleanExtra("dealConfirmed", false) == true) {
            val itemName = data.getStringExtra("itemName") ?: ""
            val isWriter = data.getBooleanExtra("isWriter", false)

            // 🔸 Toolbar 타이틀에서 닉네임 가져오기
            val opponentNickname = findViewById<Toolbar>(R.id.chatTopToolbar).title?.toString() ?: "상대방"

            showDealConfirmedUI(isWriter, itemName, opponentNickname)
        }
    }


    private fun showDealConfirmedUI(
        isWriter: Boolean, // 내가 작성자인지
        itemName: String,  // 예: "두루마리 휴지"
        opponentNickname: String // 예: "홍길동"
    ) {
        findViewById<ImageView>(R.id.img_bottom_box).visibility = View.VISIBLE
        findViewById<TextView>(R.id.cancel_notification).apply {
            visibility = View.VISIBLE
            text = if (isWriter) {
                "\"$itemName\"의 거래 확정 폼을 작성했어요."
            } else {
                "$opponentNickname 님이 \"$itemName\"의 거래 확정 폼을 작성했어요."
            }
        }
        findViewById<ImageView>(R.id.check_deal_cancel_btn).visibility = View.VISIBLE

        findViewById<ImageView>(R.id.check_deal_cancel_btn).setOnClickListener {
            val intent = Intent(this, CancelDealActivity::class.java)
            intent.putExtra("roomName", roomName)
            intent.putExtra("isWriter", isWriter) // 🔥 작성자 여부 전달
            startActivity(intent)

        }

    }



// 수정 필요
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
                handleIncomingWebSocketMessage(message)
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

    private fun handleIncomingWebSocketMessage(message: ChatMessage) {
        try {
            // ChatMessage.content는 전체 JSON 문자열이어야 함
            val json = JSONObject(message.content)

            val contentType = json.optString("content")
            if (contentType == "거래확정 폼이 작성 됨") {
                val itemName = json.optString("itemName", "물품")
                val senderId = json.optInt("sendMemberId", -1)

                val isWriter = senderId == myMemberId
                val opponentNickname = toolbarBinding.title.text.toString()

                showDealConfirmedUI(isWriter, itemName, opponentNickname)
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "거래확정 메시지 파싱 실패: ${e.message}")
        }
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
            .replace(
                R.id.chatMenuContainer,
                ChatMenuFragment.newInstance(chatRoomId, roomName)  // 👈 넘기는 건 type만
            )
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

