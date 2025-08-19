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
import com.example.onenthapp.MyReviewActivity
import com.example.onenthapp.NwonSavedActivity
import com.example.onenthapp.R
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.WriteReviewActivity
import com.example.onenthapp.chat.CancelDealActivity
import com.example.onenthapp.data.chat.ChatMessage
import com.example.onenthapp.databinding.ActivityChatRoomBinding
import com.example.onenthapp.databinding.ChatTopToolbarBinding
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
class ChatRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var webSocketClient: ChatWebSocketClient
    private lateinit var toolbarBinding: ChatTopToolbarBinding
    private var dealConfirmationId: Int = -1
    private val myMemberId: Int by lazy {
        TokenManager.getMemberId()?.toInt() ?: run {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            0  // 혹은 적절한 기본값(0 등)
        }
    }
    private var completedItemId: Int = -1
    private var completedItemImageUrl: String = ""
    private var completedItemType: String = ""
    private var completedItemTypeAndId: String = ""

    private var chatRoomId: Int = -1
    private var peerNickname: String = "익명"
    private var roomName: String = ""

    private var completedItemName: String = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            if (data.getBooleanExtra("dealConfirmed", false)) {
                val itemName = data.getStringExtra("itemName") ?: ""
                val isWriter = data.getBooleanExtra("isWriter", false)
                val opponentNickname = toolbarBinding.title.text.toString()

                showDealConfirmedUI(isWriter, itemName, opponentNickname)
            }

            // ✅ 거래 완료 후 상품 정보 처리
            val itemId = data.getIntExtra("itemId", -1)
            val itemImageUrl = data.getStringExtra("itemImageUrl") ?: ""
            val itemType = data.getStringExtra("itemType") ?: ""
            val itemTypeAndId = data.getStringExtra("itemTypeAndId") ?: ""
            val itemNameFromResult = data.getStringExtra("itemName") ?: ""  // ★ 추가

            if (itemId != -1 && itemTypeAndId.isNotEmpty()) {
                completedItemId = itemId
                completedItemImageUrl = itemImageUrl
                completedItemType = itemType
                completedItemTypeAndId = itemTypeAndId
                completedItemName = itemNameFromResult              // ★ 추가
            }

        }
    }

    private fun goToReviewPage(
        itemId: Int,
        itemImageUrl: String,
        itemType: String,
        itemTypeAndId: String
    ) {
        val intent = Intent(this, WriteReviewActivity::class.java).apply {
            putExtra("itemId", itemId)
            putExtra("itemImageUrl", itemImageUrl)
            putExtra("itemType", itemType)
            putExtra("itemTypeAndId", itemTypeAndId)
        }
        startActivity(intent)
    }

    fun handleChatBlockResult(data: Intent?) {
        if (data == null) return

        val itemId = data.getIntExtra("itemId", -1)
        val itemType = data.getStringExtra("itemType") ?: ""

        if (itemId != -1 && itemType.isNotEmpty()) {
            // 예: 결과 받았을 때 토스트 띄우기
            // 필요한 경우 화면 이동이나 UI 업데이트도 여기서 처리 가능
            // 예시)
            // val intent = Intent(this, SomeActivity::class.java)
            // startActivity(intent)

        }
    }

    private fun showDealConfirmedUI(
        isWriter: Boolean,
        itemName: String,
        opponentNickname: String
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
            Log.d("ChatRoomActivity", "Starting CancelDealActivity with dealConfirmationId: $dealConfirmationId")
            if (dealConfirmationId == -1) {
                Toast.makeText(this, "거래 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, CancelDealActivity::class.java)
            intent.putExtra("roomName", roomName)
            intent.putExtra("isWriter", isWriter)
            intent.putExtra("peerNickname", opponentNickname)
            intent.putExtra("dealConfirmationId", dealConfirmationId)
            startActivity(intent)
        }
    }
    private fun showDealCompleteUI(
        isWriter: Boolean,
        itemName: String,
        opponentNickname: String,
        itemId: Int,
        itemType: String
    ) {
        // 값 저장
        completedItemId = itemId
        completedItemType = itemType

        findViewById<ImageView>(R.id.img_bottom_box).visibility = View.VISIBLE
        // 알림 텍스트
        findViewById<TextView>(R.id.cancel_notification).apply {
            visibility = View.VISIBLE
            text = if (isWriter) {
                "\"$itemName\"의 거래가 완료되었어요."
            } else {
                "$opponentNickname 님과의 \"$itemName\" 거래가 완료되었어요."
            }
        }

        // ✅ 거래완료 관련 뷰 보이기
        val reviewButton = findViewById<ImageView>(R.id.completereivew)
        val nwonSaveButton = findViewById<ImageView>(R.id.completensave)

        reviewButton.visibility = View.VISIBLE
        nwonSaveButton.visibility = View.VISIBLE

        // ❌ 거래확정 관련 버튼 숨기기
        findViewById<ImageView>(R.id.check_deal_cancel_btn).visibility = View.GONE

        // 💬 후기 남기러 가기
        reviewButton.setOnClickListener {
            Log.d("ChatRoomActivity", "리뷰 버튼 클릭됨 - itemId: $completedItemId, itemType: $completedItemType")

            if (completedItemId != -1) {
                val intent = Intent(this, WriteReviewActivity::class.java).apply {
                    putExtra("itemId", completedItemId)
                    putExtra("itemType", completedItemType)
                    putExtra("itemName", completedItemName)             // ★ 추가
                    putExtra("itemImageUrl", completedItemImageUrl)     // ★ 추가
                    putExtra("itemTypeAndId", completedItemTypeAndId)   // (있으면 같이)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "아이템 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 💬 누적 내역 보기
        nwonSaveButton.setOnClickListener {
            val intent = Intent(this, NwonSavedActivity::class.java)
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
            val json = JSONObject(message.content)
            val contentType = json.optString("content")
            val itemName = json.optString("itemName", "물품")
            val senderId = json.optInt("sendMemberId", -1)
            val isWriter = senderId == myMemberId
            val opponentNickname = toolbarBinding.title.text.toString()
            when (contentType) {
                "거래확정 폼이 작성 됨" -> {
                    val dealConfirmationFormId = json.getInt("dealConfirmationFormId")
                    dealConfirmationId = dealConfirmationFormId
                    showDealConfirmedUI(isWriter, itemName, opponentNickname)
                }
                "거래완료 폼이 작성 됨" -> {
                    val itemId = json.optInt("itemId", -1)
                    val itemType = json.optString("itemType", "")
                    showDealCompleteUI(isWriter, itemName, opponentNickname, itemId, itemType)
                }

                else -> {
                    // 일반 메시지로 처리하거나 무시
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "거래 메시지 파싱 실패: ${e.message}")
        }
    }
    private fun setupToolbar(peerNickname: String) {
        toolbarBinding.title.text = peerNickname
        toolbarBinding.btnLeft.setOnClickListener { finish() }
        toolbarBinding.btnMenu.setOnClickListener { showChatMenu() }
    }
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(myMemberId, peerNickname)
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
    fun onDealConfirmedMessage(rawJson: String, isWriter: Boolean, itemName: String, opponentNickname: String) {
        try {
            // JSON 파싱 (예: org.json.JSONObject 사용)
            val json = JSONObject(rawJson)
            val dealConfirmationFormId = json.getInt("dealConfirmationFormId")
            // 변수에 저장
            dealConfirmationId = dealConfirmationFormId
            // UI 보여주기 호출 (필요한 값 같이 전달)
            showDealConfirmedUI(isWriter, itemName, opponentNickname)
        } catch (e: JSONException) {
            e.printStackTrace()
            // 파싱 실패 처리
        }
    }
}