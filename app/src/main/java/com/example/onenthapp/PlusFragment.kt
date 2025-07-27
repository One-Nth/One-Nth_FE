package com.example.onenthapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.onenthapp.databinding.FragmentPlusBinding

class PlusFragment : Fragment() {
    private var _binding: FragmentPlusBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
}