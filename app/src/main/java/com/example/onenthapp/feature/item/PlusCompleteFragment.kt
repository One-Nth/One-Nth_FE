package com.example.onenthapp.feature.item

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.onenthapp.R
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
        //val imageUri = requireArguments().getString("productImageUri", "")
        val productId = requireArguments().getLong("productId")
        val isBuy = requireArguments().getBoolean("isBuy", true)
        val firstImageUrl = requireArguments().getString("firstImageUrl", "")

        // 2) 텍스트 뷰에 설정
        binding.tvProductName.text = name
        binding.tvProductPrice.text = "${price}원"

        
        // 4) 첫 번째 이미지 바인딩
        if (firstImageUrl.isNotEmpty()) {
            try {
                // Glide를 사용하여 이미지 로딩
                Glide.with(this)
                    .load(firstImageUrl)
                    .placeholder(R.drawable.image_placeholder_bg) // 로딩 중 기본 이미지
                    .error(R.drawable.image_placeholder_bg) // 에러 시 기본 이미지
                    .into(binding.previewImageBox)
            } catch (e: Exception) {
                // 이미지 로딩 실패 시 아무것도 하지 않음
                Log.e("PlusComplete", "이미지 로딩 실패: ${e.message}")
            }
        }
        // 닫기(X) 누르면 홈으로
        binding.btnCloseComplete.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
        binding.previewContainer.setOnClickListener {
            if (isBuy) {
                findNavController().navigate(
                    R.id.groupPurchaseDetailFragment,
                    bundleOf("productId" to productId)
                )
            } else {
                findNavController().navigate(
                    R.id.sharingItemDetailFragment,
                    bundleOf("productId" to productId)
                )
            }
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
