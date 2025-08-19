package com.example.onenthapp.feature.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.onenthapp.databinding.FragmentChatMenuBinding

class ChatMenuFragment : Fragment() {

    companion object {
        private const val ARG_CHAT_ROOM_ID = "chatRoomId"
        private const val ARG_ROOM_NAME = "roomName"

        fun newInstance(chatRoomId: Int, roomName: String): ChatMenuFragment {
            val fragment = ChatMenuFragment()
            val args = Bundle()
            args.putInt(ARG_CHAT_ROOM_ID, chatRoomId)
            args.putString(ARG_ROOM_NAME, roomName)
            fragment.arguments = args
            return fragment
        }
    }

    private var chatRoomId: Int = -1
    private lateinit var roomName: String

    private var _binding: FragmentChatMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatRoomId = arguments?.getInt(ARG_CHAT_ROOM_ID) ?: -1
        roomName = arguments?.getString(ARG_ROOM_NAME) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chatRoomType = roomName.substringAfterLast("-")

        if (chatRoomType == "TIP_SHARE") {
            binding.chatMenuCheck.visibility = View.GONE
            binding.chatMenuBlock.visibility = View.GONE
        }

        // 🔘 알람 설정
        binding.chatMenuAlarm.setOnClickListener {
            BottomChatActionDialogFragment(BottomChatActionDialogFragment.ActionType.MUTE)
                .show(parentFragmentManager, "MuteDialog")
        }

        // 🔘 체크 리스트 이동
        binding.chatMenuCheck.setOnClickListener {
            val intent = Intent(requireContext(), ChatCheckActivity::class.java)
            intent.putExtra("roomName", roomName)
            startActivity(intent)
        }

        // 🔘 차단
        binding.chatMenuBlock.setOnClickListener {
            val intent = Intent(requireContext(), ChatBlockActivity::class.java)
            intent.putExtra("roomName", roomName)
            chatBlockLauncher.launch(intent)
        }

        // 🔘 신고
        binding.chatMenuDeclare.setOnClickListener {
            BottomChatActionDialogFragment(BottomChatActionDialogFragment.ActionType.REPORT)
                .show(parentFragmentManager, "DeclareDialog")
        }

        // 🔘 채팅방 나가기
        binding.chatMenuExit.setOnClickListener {
            BottomChatActionDialogFragment.newInstance(
                chatRoomId,
                roomName,
                BottomChatActionDialogFragment.ActionType.EXIT
            ).show(parentFragmentManager, "ExitDialog")
        }
    }
    private val chatBlockLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data = result.data
            // 결과를 Activity에 전달
            (activity as? ChatRoomActivity)?.handleChatBlockResult(data)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
