package com.example.onenthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.onenthapp.data.GroupPurchaseDetailResult
import com.example.onenthapp.data.PlusRepository
import com.example.onenthapp.databinding.FragmentProductDetailBinding
import kotlinx.coroutines.launch

// GroupPurchaseDetailFragment.kt
class GroupPurchaseDetailFragment : Fragment() {
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val repo = PlusRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentProductDetailBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productId = requireArguments().getLong("productId")

        lifecycleScope.launch {
            try {
                val resp = repo.fetchGroupPurchaseDetail(productId)
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    bindDetail(resp.body()!!.result)
                } else {
                    Toast.makeText(requireContext(), "불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun bindDetail(d: GroupPurchaseDetailResult?) {
        val categoryLabel =
            when(d?.itemCategory) {
                "FOOD" -> "식품"
                "ELECTRONICS" -> "전자기기"
                "HOUSEHOLD" -> "생활용품"
                "CLOTHING" -> "의류"
                "MISC" -> "잡화"
                else -> d?.itemCategory
            }
        with(binding) {
            tvTitle.text         = d?.title
            tvProductName.text = d?.title
            tvProductLink.text   = d?.purchaseUrl
            //tvQuantity.text      = "${d.quantity}개"
            tvPrice.text         = "${d?.price}원"
            tvCategory.text      = categoryLabel
            tvProductDue.text    = d?.expirationDate ?: "없음"
            tvMethod.text        = if(d?.purchaseMethod == "OFFLINE") "직거래" else "온라인"
            tvSellerName.text        = d?.writerNickname
            tvStatus.text        = d?.statusLabel

            // 이미지 carousel
            //imageCarousel.setImageUrls(d.imageUrls)
            // 태그
//            chipGroupTags.removeAllViews()
//            d.tags.forEach { tag ->
//                val chip = Chip(requireContext()).apply { text = tag }
//                chipGroupTags.addView(chip)
//            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
