package com.example.onenthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.onenthapp.databinding.FragmentPlusBuyBinding
import androidx.navigation.fragment.findNavController

class PlusBuyFragment : Fragment() {
    private var _binding: FragmentPlusBuyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlusBuyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnProductSubmit.setOnClickListener {
            // 1) 폼 값 읽기
            val name = binding.etProductName.text.toString()
            val price = binding.etProductOrigincost.text.toString()
//            // 만약 이미지 피커를 구현하셨다면, URI 를 String 으로 꺼내세요.
//            val imageUri = binding.ivPreviewImage.drawable.let {
//                // 예시: 실제 URI 를 String 으로 저장해두셨다면 여기에 꺼내서 넣어주세요.
//                ""
//            }
            val imageUri = "imageUri"

            // 2) Bundle 에 담아서 navigate
            val bundle = bundleOf(
                "productName" to name,
                "productPrice" to price,
                "productImageUri" to imageUri
            )
            findNavController().navigate(R.id.action_global_complete, bundle)
        }
        binding.includeToolbar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        setupToggleButtons()
    }
    private fun setupToggleButtons() {
        binding.btnWay1.setOnClickListener {
            binding.btnWay1.isChecked = true
            binding.btnWay2.isChecked = false
        }
        binding.btnWay2.setOnClickListener {
            binding.btnWay2.isChecked = true
            binding.btnWay1.isChecked = false
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}