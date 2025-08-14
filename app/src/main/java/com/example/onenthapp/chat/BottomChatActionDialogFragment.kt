package com.example.onenthapp.chat

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.onenthapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class BottomChatActionDialogFragment(
    private val type: ActionType
) : BottomSheetDialogFragment() {

    enum class ActionType {
        REPORT,  MUTE, EXIT
    }

    override fun getTheme(): Int = R.style.BottomSheet_NoDim_HalfHeight

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
                button.text = "차단하기"
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

                }
            }
        }

        button.setOnClickListener { dismiss() }

        return view
    }

    override fun onStart() {
        super.onStart()
        Log.d("BottomSheetTest", "onStart 호출됨")

        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
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