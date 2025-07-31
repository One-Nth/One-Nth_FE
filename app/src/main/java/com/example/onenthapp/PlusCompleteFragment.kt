package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.onenthapp.databinding.FragmentPlusCompleteBinding

class PlusCompleteFragment : Fragment() {
    private var _binding: FragmentPlusCompleteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentPlusCompleteBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) 인자 꺼내기
        val name = requireArguments().getString("productName", "")
        val price = requireArguments().getString("productPrice", "")
        val imageUri = requireArguments().getString("productImageUri", "")

        // 2) 텍스트 뷰에 설정
        binding.tvProductName.text = name
        binding.tvProductPrice.text = "${price}원"
//        if (imageUri.isEmpty()) {
//            binding.previewImageBox.background = "@drawable/image_tissue_1"
//        }
        // 닫기(X) 누르면 홈으로
        binding.btnCloseComplete.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }

//        // 공유하기
//        binding.shareContainer.setOnClickListener {
//            val shareText = "${binding.tvPreviewTitle.text}\n${binding.tvPreviewPrice.text}"
//            Intent(Intent.ACTION_SEND).apply {
//                type = "text/plain"
//                putExtra(Intent.EXTRA_TEXT, shareText)
//            }.also { startActivity(Intent.createChooser(it, getString(R.string.share_via))) }
//        }

        // 확인 누르면 홈으로
        binding.tvConfirm.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
