package com.example.onenthapp.feature.chat

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.onenthapp.R
import com.example.onenthapp.RetrofitInstance
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class BottomChatActionDialogFragment(
    private val type: ActionType
) : BottomSheetDialogFragment() {

    enum class ActionType {
        REPORT,  MUTE, EXIT
    }
    private var chatRoomId: Int = -1
    private lateinit var roomName: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatRoomId = arguments?.getInt(ARG_CHAT_ROOM_ID) ?: -1
        roomName = arguments?.getString(ARG_ROOM_NAME) ?: "" // <- roomName 받아오기
    }

        companion object {
            private const val ARG_CHAT_ROOM_ID = "chatRoomId"
            private const val ARG_ROOM_NAME = "roomName"
            private const val ARG_ACTION_TYPE = "actionType"

            fun newInstance(chatRoomId: Int, roomName: String, actionType: ActionType): BottomChatActionDialogFragment {
                val fragment = BottomChatActionDialogFragment(actionType)
                val args = Bundle()
                args.putInt(ARG_CHAT_ROOM_ID, chatRoomId)
                args.putString(ARG_ROOM_NAME, roomName)
                fragment.arguments = args
                return fragment
            }

        }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_chat_action, container, false)

        val title = view.findViewById<TextView>(R.id.titleText)
        val description = view.findViewById<TextView>(R.id.descriptionText)
        val button = view.findViewById<Button>(R.id.actionButton)

        when (type) {
            ActionType.REPORT -> {
                title.text = "해당 사용자를 신고하는 이유를 골라주세요."
                description.visibility = View.GONE
                view.findViewById<View>(R.id.reportOptionsGroup).visibility = View.VISIBLE
                button.text = "신고하기"
                button.setBackgroundColor(Color.parseColor("#FF8383"))
            }
            ActionType.MUTE -> {
                title.text = "채팅 알람을 해제하시겠습니까?"
                description.text = "채팅 알람을 해제하면 거래 확정 및 완료 알림을\n받아볼 수 없어요."
                button.text = "작성하기"
            }
            ActionType.EXIT -> {
                title.text = "해당 채팅방을 나가시겠습니까?"
                description.text = "해당 채팅방 나가기 시 채팅방의 데이터가 모두\n사라집니다."
                button.text = "나가기"

                button.setOnClickListener {
                    val chatRoomId = arguments?.getInt(ARG_CHAT_ROOM_ID) ?: -1
                    leaveChatRoom(chatRoomId)

                }
            }
        }

        return view
    }

    private fun leaveChatRoom(chatRoomId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.messageApi.leaveChatRoom(chatRoomId)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "채팅방을 나갔습니다.", Toast.LENGTH_SHORT).show()
                    dismiss()
                    activity?.finish() // ChatRoomActivity 종료
                } else {
                    Log.e("ChatRoomExit", "나가기 실패: ${response.code()}")
                    Toast.makeText(requireContext(), "채팅방 나가기에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChatRoomExit", "에러: ${e.message}")
                Toast.makeText(requireContext(), "에러 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("BottomSheetTest", "onStart 호출됨")

        dialog?.let {
            val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                Log.d("BottomSheetTest", "sheet found")

                val layoutParams = sheet.layoutParams
                layoutParams.height = (resources.displayMetrics.heightPixels * 0.5).toInt()
                sheet.layoutParams = layoutParams

                sheet.setBackgroundColor(Color.TRANSPARENT)
            } ?: Log.e("BottomSheetTest", "sheet == null")
        } ?: Log.e("BottomSheetTest", "dialog == null")
    }


}