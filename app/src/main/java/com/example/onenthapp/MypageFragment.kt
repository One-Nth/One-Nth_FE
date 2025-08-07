package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class MypageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mypage, container, false)

        val settingsButton = view.findViewById<ImageView>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        val policyButton = view.findViewById<LinearLayout>(R.id.menuPolicy)
        policyButton.setOnClickListener {
            val intent = Intent(requireContext(), PolicyActivity::class.java)
            startActivity(intent)
        }

        // ✅ 스크랩한 글 클릭 시 이동
        val scrapButton = view.findViewById<LinearLayout>(R.id.menuScrap)
        scrapButton.setOnClickListener {
            val intent = Intent(requireContext(), ScrapActivity::class.java)
            startActivity(intent)
        }

        // ✅ 공감한 글 클릭 시 이동
        val likePostButton = view.findViewById<LinearLayout>(R.id.menuLike)
        likePostButton.setOnClickListener {
            val intent = Intent(requireContext(), LikePostActivity::class.java)
            startActivity(intent)
        }

        // ✅ 내가 쓴 글 클릭 시 이동
        val writtenButton = view.findViewById<LinearLayout>(R.id.menuWritten)
        writtenButton.setOnClickListener {
            val intent = Intent(requireContext(), MyPostActivity::class.java)
            startActivity(intent)
        }

        // ✅ 내가 쓴 글 클릭 시 이동
        val ReviewButton = view.findViewById<LinearLayout>(R.id.menuEditReview)
        ReviewButton.setOnClickListener {
            val intent = Intent(requireContext(), AllMyReviewActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
