package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.onenthapp.databinding.BottomChatMenuBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.databinding.ActivityChatRoomBinding
import com.example.onenthapp.databinding.ChatTopToolbarBinding

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var chatAdapter: ChatAdapter

    private val mockMessages = listOf(
        ChatMessage("유저", "안녕하세요!"),
        ChatMessage("관리자", "안녕하세요, 꿀팁을 알려드릴게요."),
        ChatMessage("유저", "좋아요!"),
        ChatMessage("관리자", "앱을 껐다 켜면 더 빨라집니다 :)")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView 설정
        chatAdapter = ChatAdapter(mockMessages)
        binding.chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ChatRoomActivity)
        }

        val toolbarBinding = ChatTopToolbarBinding.bind(binding.chatTopToolbar.getChildAt(0))
        toolbarBinding.btnLeft.setOnClickListener {
            finish()
        }

        val btnMenu = findViewById<ImageButton>(R.id.btn_menu)
        btnMenu.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
            val bottomSheetBinding = BottomChatMenuBinding.inflate(layoutInflater)

            bottomSheetDialog.setContentView(bottomSheetBinding.root)

// 높이 강제 설정
            bottomSheetBinding.root.post {
                val bottomSheet = bottomSheetDialog.delegate.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                ) as FrameLayout

// 화면 높이의 50%로 설정
                val displayMetrics = resources.displayMetrics
                val halfHeight = (displayMetrics.heightPixels * 0.5).toInt()
                bottomSheet.layoutParams.height = halfHeight

                bottomSheet.setBackgroundResource(R.drawable.bg_bottom_sheet)
            }

            bottomSheetBinding.chatMenuAlarm.setOnClickListener {
                bottomSheetDialog.dismiss()
                BottomChatActionDialogFragment(BottomChatActionDialogFragment.ActionType.MUTE)
                    .show(supportFragmentManager, "MuteDialog")
            }

            bottomSheetBinding.chatMenuCheck.setOnClickListener {
                bottomSheetDialog.dismiss()
                val intent = Intent(this@ChatRoomActivity, ChatCheckActivity::class.java)
                startActivity(intent)
            }

            bottomSheetBinding.chatMenuBlock.setOnClickListener {
                bottomSheetDialog.dismiss()
                BottomChatActionDialogFragment(BottomChatActionDialogFragment.ActionType.BLOCK)
                    .show(supportFragmentManager, "BlockDialog")
            }

            bottomSheetBinding.chatMenuDeclare.setOnClickListener {
                bottomSheetDialog.dismiss()
                BottomChatActionDialogFragment(BottomChatActionDialogFragment.ActionType.REPORT)
                    .show(supportFragmentManager, "DeclareDialog")
            }

            bottomSheetBinding.chatMenuExit.setOnClickListener {
                bottomSheetDialog.dismiss()
                BottomChatActionDialogFragment(BottomChatActionDialogFragment.ActionType.EXIT)
                    .show(supportFragmentManager, "ExitDialog")
            }

            bottomSheetDialog.setContentView(bottomSheetBinding.root)
            bottomSheetDialog.show()
        }
        }

    }
