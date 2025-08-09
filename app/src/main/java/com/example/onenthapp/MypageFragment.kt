package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch

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

        // ✅ 나의 거래내역 및 후기 클릭 시 이동
        val DealButton = view.findViewById<LinearLayout>(R.id.menuReview)
        DealButton.setOnClickListener {
            val intent = Intent(requireContext(), MyPageReviewsActivity::class.java)
            startActivity(intent)
        }

        // ✅ 내가 쓴 후기 수정하기 클릭 시 이동
        val ReviewButton = view.findViewById<LinearLayout>(R.id.menuEditReview)
        ReviewButton.setOnClickListener {
            val intent = Intent(requireContext(), AllMyReviewActivity::class.java)
            startActivity(intent)
        }

        val ArrowButton = view.findViewById<ImageView>(R.id.profileArrow)
        ArrowButton.setOnClickListener {
            val intent = Intent(requireContext(), AccountSettingsActivity::class.java)
            startActivity(intent)
        }

        //프로필 불러오기
        bindProfile(view)

        return view
    }


    // 프로필 이미지/닉네임 연동
    private fun bindProfile(root: View) {
        val iv = root.findViewById<ImageView>(R.id.profileImage)
        val tv = root.findViewById<TextView>(R.id.nickname)

        // 토큰 없으면 기본 UI 유지
        val token = TokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            // 필요하면 로그인 유도 토스트/화면 전환
            // Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.memberApi.getProfile()
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val p = resp.body()!!.result
                    // 닉네임
                    tv.text = p.nickname ?: "닉네임"
                    // 프로필 이미지
                    val url = p.profileImageUrl
                    if (!url.isNullOrBlank()) {
                        Glide.with(this@MypageFragment)
                            .load(url)
                            .placeholder(R.drawable.profile_base) // 기본 이미지
                            .error(R.drawable.profile_base)
                            .circleCrop()
                            .into(iv)
                    } else {
                        iv.setImageResource(R.drawable.profile_base)
                    }
                } else {
                    Log.e("MypageFragment", "프로필 실패: ${resp.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("MypageFragment", "프로필 로드 오류: ${e.message}")
            }
        }
    }
}
