package com.example.onenthapp

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar

class StatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_nwon_saved, container, false)


//        val transactionTitle = view.findViewById<TextView>(R.id.transactionTitle)
//        transactionTitle.setOnClickListener {
//            val intent = Intent(requireContext(), MyReviewActivity::class.java)
//            startActivity(intent)
//        }
        val transactionLayout = view.findViewById<LinearLayout>(R.id.transactionLayout)
        transactionLayout.setOnClickListener {
            val intent = Intent(requireContext(), MyReviewActivity::class.java)
            startActivity(intent)
        }

        // 공유 버튼 눌렀을 때 팝업 띄우기
        val shareButton = view.findViewById<View>(R.id.btn_share)
        shareButton.setOnClickListener {
            showShareDialog()
        }

        val topAppBar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }


        return view
    }

    private fun showShareDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.share_nwon_popup, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // 닫기 버튼
        val closeButton = dialogView.findViewById<TextView>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        // 링크 입력창 & 복사 버튼
        val linkEditText = dialogView.findViewById<EditText>(R.id.shareLinkEditText)
        val copyButton = dialogView.findViewById<ImageButton>(R.id.copyButton)

        copyButton.setOnClickListener {
            val text = linkEditText.text.toString()
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("공유링크", text))
            Toast.makeText(requireContext(), "링크가 복사되었습니다", Toast.LENGTH_SHORT).show()
        }

        dialog.show()

        // 팝업 크기 강제 조정
        val widthInPx = (347 * resources.displayMetrics.density).toInt()
        val heightInPx = (202 * resources.displayMetrics.density).toInt()
        dialog.window?.setLayout(widthInPx, heightInPx)

        // 배경 투명화 (둥근 테두리 유지)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
