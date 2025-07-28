package com.example.onenthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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