package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.databinding.FragmentTipsLifetipsBinding
import com.example.onenthapp.data.post.TipItem


class LifeTipsFragment : Fragment() {
    private var _binding: FragmentTipsLifetipsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LifeTipsSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTipsLifetipsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 1) 어댑터 생성 (onClick 콜백만 전달)
        adapter = LifeTipsSearchAdapter { tipItem ->
            val intent = Intent(requireContext(), LifeTipsDetailActivity::class.java).apply {
                putExtra("title", tipItem.title)
                putExtra("content", tipItem.content)
                putExtra("timeAgo", tipItem.timeAgo)
                putExtra("commentCount", tipItem.commentCount)
                putExtra("likeCount", tipItem.likeCount)
                putExtra("viewCount", tipItem.viewCount)
            }
            startActivity(intent)
        }

        // 2) 리사이클러뷰 붙이기
        binding.rvLifeTips.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@LifeTipsFragment.adapter
        }

        // 3) 데이터 주입 (임시 더미)
//        val dummyList = listOf(
//            TipItem("곰팡이 제거제", "000 곰팡이 제거제 어떤가요", "3분 전", 2, 1, 24),
//            TipItem("모기약", "효과 어떤가요?", "10분 전", 1, 0, 15),
//            TipItem("방향제", "차량용 추천해주세요", "1시간 전", 4, 3, 50)
//        )
//        adapter.submitItems(dummyList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
