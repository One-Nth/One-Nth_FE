package com.example.onenthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.onenthapp.databinding.FragmentPlusShareBinding
import androidx.navigation.fragment.findNavController

class PlusShareFragment : Fragment() {
    private var _binding: FragmentPlusShareBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlusShareBinding.inflate(inflater, container, false)
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
        binding.btnConfirmYes.setOnClickListener {
            binding.btnConfirmYes.isChecked = true
            binding.btnConfirmNoCancel.isChecked = false
        }
        binding.btnConfirmNoCancel.setOnClickListener {
            binding.btnConfirmNoCancel.isChecked = true
            binding.btnConfirmYes.isChecked = false
        }
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