package com.example.onenthapp.feature.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.databinding.FragmentTipChatBinding
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

        fetchChatRooms("TIP_SHARE")

        chatNotificationAdapter.setOnItemClickListener { chatNotification ->
            val intent = Intent(requireContext(), ChatRoomActivity::class.java).apply {
                putExtra("chatRoomId", chatNotification.chatRoomId)
                putExtra("myMemberId", TokenManager.getMemberId())
                putExtra("roomName", chatNotification.roomName)
                putExtra("peerNickname", chatNotification.nickname)
                putExtra("opponentId", chatNotification.opponentId)
            }
            startActivity(intent)
        }
    }

    private fun fetchChatRooms(chatRoomType: String) {
        lifecycleScope.launch {
            try {
                // 차단 닉네임 목록 불러오기
                val blockedNicknames = withContext(Dispatchers.IO) {
                    try {
                        val res = RetrofitInstance.usersetApi.getBlockedUsers()
                        if (res.isSuccessful && res.body()?.isSuccess == true) {
                            res.body()?.result?.blockedUserSummaryList?.map { it.nickname } ?: emptyList()
                        } else emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                val response = withContext(Dispatchers.IO) {
                    api.getChatListMessages(chatRoomType)
                }

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val chatRooms = response.body()?.result ?: emptyList()

                    val notifications = withContext(Dispatchers.IO) {
                        chatRooms.map { chatRoom ->
                            async {
                                val nickname = try {
                                    val res = api.getMemberNickname(chatRoom.opponentId)
                                    if (res.isSuccessful && res.body()?.isSuccess == true) {
                                        res.body()?.result?.nickname ?: "알 수 없음"
                                    } else "알 수 없음"
                                } catch (e: Exception) {
                                    "알 수 없음"
                                }

                                val isBlocked = blockedNicknames.contains(nickname)

                                ChatNotification(
                                    chatRoomId = chatRoom.chatRoomId,
                                    nickname = nickname,
                                    message = chatRoom.lastMessageContent ?: "메시지 없음",
                                    time = formatTime(chatRoom.lastMessageTime),
                                    roomName = chatRoom.chatRoomName,
                                    opponentId = chatRoom.opponentId,
                                    isBlocked = isBlocked
                                )
                            }
                        }.awaitAll()
                    }

                    chatNotificationAdapter.updateData(notifications)
                } else {
                    Toast.makeText(requireContext(), "채팅 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException) {
                Toast.makeText(requireContext(), "서버 오류", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "알 수 없는 오류 발생", Toast.LENGTH_SHORT).show()
                Log.e("fetchChatRooms", "알 수 없는 오류 발생", e)
            }
        }
    }

    private fun formatTime(iso: String?): String {
        if (iso.isNullOrEmpty()) return ""

        return try {
            val trimmed = iso.substringBefore('.')
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = sdf.parse(trimmed) ?: return trimmed

            val diffMs = System.currentTimeMillis() - date.time
            val mins = diffMs / 60000
            val hours = mins / 60
            val days = hours / 24

            when {
                mins < 1 -> "방금 전"
                mins < 60 -> "${mins}분 전"
                hours < 24 -> "${hours}시간 전"
                days < 7 -> "${days}일 전"
                else -> trimmed.replace('T', ' ')
            }
        } catch (_: Exception) {
            iso.substringBefore('.').replace('T', ' ')
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
