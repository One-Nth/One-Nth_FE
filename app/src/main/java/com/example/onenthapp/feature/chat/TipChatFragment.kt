package com.example.onenthapp.feature.chat

import ChatNotification
import ChatNotificationAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.databinding.FragmentTipChatBinding
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class TipChatFragment : Fragment() {

    private var _binding: FragmentTipChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatNotificationAdapter: ChatNotificationAdapter
    private val api = RetrofitInstance.messageApi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTipChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatNotificationAdapter = ChatNotificationAdapter(emptyList(), requireContext())
        binding.notificationList.apply {
            adapter = chatNotificationAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        fetchChatRooms("TIP_SHARE") // TIP_SHARE 타입 채팅방 목록 불러오기

        // RecyclerView 아이템 클릭 리스너 (아이템 클릭 시 ChatRoomActivity로 이동)
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
                    api.getChatListMessages(chatRoomType)
                }

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val chatRooms = response.body()?.result ?: emptyList()

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

    private fun formatTime(isoTime: String): String {
        return isoTime.replace("T", " ").substring(0, 16)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
