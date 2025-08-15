package com.example.onenthapp.utils

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.onenthapp.R

object ShareDialogUtil {
    
    fun showShareDialog(
        context: Context,
        shareUrl: String = "",
        title: String = "해당 글을 공유하시겠습니까?"
    ) {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.share_nwon_popup, null)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        // 제목 설정
        val shareTitle = dialogView.findViewById<TextView>(R.id.shareTitle)
        shareTitle.text = title

        // 닫기 버튼
        val closeButton = dialogView.findViewById<TextView>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        // 링크 입력창 & 복사 버튼
        val linkEditText = dialogView.findViewById<EditText>(R.id.shareLinkEditText)
        val copyButton = dialogView.findViewById<ImageButton>(R.id.copyButton)

        // 공유 URL 설정
        linkEditText.setText(shareUrl)

        copyButton.setOnClickListener {
            val text = linkEditText.text.toString()
            if (text.isNotEmpty()) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("공유링크", text))
                Toast.makeText(context, "링크가 복사되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "복사할 링크가 없습니다", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()

        // 팝업 크기 강제 조정
        val resources = context.resources
        val widthInPx = (347 * resources.displayMetrics.density).toInt()
        val heightInPx = (202 * resources.displayMetrics.density).toInt()
        dialog.window?.setLayout(widthInPx, heightInPx)

        // 배경 투명화 (둥근 테두리 유지)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }


    fun Fragment.showShareDialog(
        shareUrl: String = "",
        title: String = "해당 글을 공유하시겠습니까?"
    ) {
        showShareDialog(requireContext(), shareUrl, title)
    }
}
