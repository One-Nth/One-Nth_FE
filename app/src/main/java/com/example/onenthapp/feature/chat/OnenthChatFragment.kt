package com.example.onenthapp.feature.chat

import ChatNotification
import ChatNotificationAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.onenthapp.RetrofitInstance.messageApi
import com.example.onenthapp.databinding.FragmentOnenthChatBinding
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class OnenthChatFragment : Fragment() {

    private var _binding: FragmentOnenthChatBinding? = null
    private val binding get() = _binding!!
    private val api = messageApi
    private lateinit var chatNotificationAdapter: ChatNotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnenthChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatNotificationAdapter = ChatNotificationAdapter(emptyList(), requireContext())
        binding.notificationList.layoutManager = LinearLayoutManager(requireContext()) // 꼭 필요
        binding.notificationList.adapter = chatNotificationAdapter


        // API에서 채팅방 목록 불러오기
        fetchChatRooms("DEAL")  // 정상 호출 (chatRoomType이 URL에 들어감)

        chatNotificationAdapter.setOnItemClickListener { chatNotification ->
            val intent = Intent(requireContext(), ChatRoomActivity::class.java)
            intent.putExtra("chatRoomId", chatNotification.chatRoomId) // chatRoomId 추가
            intent.putExtra("myMemberId", 1) // 본인 ID (예: 로그인 정보에서 받아서 넣기)
            startActivity(intent)
        }
    }

    private fun fetchChatRooms(chatRoomType: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    messageApi.getChatListMessages(chatRoomType)
                }

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val chatRooms = response.body()?.result ?: emptyList()

                    // ChatRoom을 ChatNotification으로 변환
                    val notifications = chatRooms.map {
                        ChatNotification(
                            nickname = it.chatRoomName,
                            message = it.lastMessageContent,
                            time = formatTime(it.lastMessageTime),
                            chatRoomId =it.chatRoomId)
                    }

                    chatNotificationAdapter.updateData(notifications)

                } else {
                    Toast.makeText(requireContext(), "채팅 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: IOException) {
                Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException) {
                Toast.makeText(requireContext(), "서버 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ISO8601 시간 → 사용자 친화적 문자열로 변환 (예: "1시간 전")
    private fun formatTime(isoTime: String): String {
        // 간단히 ISO 형식을 그대로 반환하거나, 원하는 경우 포맷팅 추가 가능
        return isoTime.replace("T", " ").substring(0, 16) // "2025-08-01 12:02"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
