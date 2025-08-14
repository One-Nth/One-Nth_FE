package com.example.onenthapp.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        binding.chatMenuAlarm.setOnClickListener {
            BottomChatActionDialogFragment(BottomChatActionDialogFragment.ActionType.MUTE)
                .show(parentFragmentManager, "MuteDialog")
        }

        binding.chatMenuCheck.setOnClickListener {
            val intent = Intent(requireContext(), ChatCheckActivity::class.java)
            intent.putExtra("roomName", roomName)  // roomName 전달
            startActivity(intent)
        }

        binding.chatMenuBlock.setOnClickListener {
            val intent = Intent(requireContext(), ChatBlockActivity::class.java)
            intent.putExtra("roomName", roomName)  // roomName 전달
            startActivity(intent)
        }


        binding.chatMenuDeclare.setOnClickListener {
            BottomChatActionDialogFragment(BottomChatActionDialogFragment.ActionType.REPORT)
                .show(parentFragmentManager, "DeclareDialog")
        }

        binding.chatMenuExit.setOnClickListener {
            BottomChatActionDialogFragment.newInstance(
                chatRoomId,
                roomName,
                BottomChatActionDialogFragment.ActionType.EXIT
            ).show(parentFragmentManager, "ExitDialog")
        }

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
