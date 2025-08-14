package com.example.onenthapp.feature.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.onenthapp.databinding.FragmentChatMenuBinding

class ChatMenuFragment : Fragment() {

    private var _binding: FragmentChatMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.chatMenuAlarm.setOnClickListener {
            BottomChatActionDialogFragment(BottomChatActionDialogFragment.ActionType.MUTE)
                .show(parentFragmentManager, "MuteDialog")
        }

        binding.chatMenuCheck.setOnClickListener {
            startActivity(Intent(requireContext(), ChatCheckActivity::class.java))
        }

        binding.chatMenuBlock.setOnClickListener {
            startActivity(Intent(requireContext(), ChatBlockActivity::class.java))
        }

        binding.chatMenuDeclare.setOnClickListener {
            BottomChatActionDialogFragment(BottomChatActionDialogFragment.ActionType.REPORT)
                .show(parentFragmentManager, "DeclareDialog")
        }

        binding.chatMenuExit.setOnClickListener {
            BottomChatActionDialogFragment(BottomChatActionDialogFragment.ActionType.EXIT)
                .show(parentFragmentManager, "ExitDialog")
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
