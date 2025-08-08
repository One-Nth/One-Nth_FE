package com.example.onenthapp.chat

import ChatNotification
import ChatNotificationAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.databinding.FragmentTipChatBinding

class TipChatFragment : Fragment() {

    private var _binding: FragmentTipChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatNotificationAdapter: ChatNotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTipChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 더미 데이터 예시
        val notifications = listOf(
            ChatNotification("닉네임1", "여기 너무 맛있다!", "2초 전"),
            ChatNotification("닉네임2", "이번 주 회식 있어요", "5분 전"),
            ChatNotification("닉네임3", "오늘 누구와 점심?", "1시간 전")
        )

        // 어댑터 초기화 및 RecyclerView 연결
        chatNotificationAdapter = ChatNotificationAdapter(notifications, requireContext())
        binding.notificationList.apply {
            adapter = chatNotificationAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // 알림 리스트 전체 클릭 시 채팅방 이동
        binding.notificationList.setOnClickListener {
            val intent = Intent(requireContext(), ChatRoomActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
