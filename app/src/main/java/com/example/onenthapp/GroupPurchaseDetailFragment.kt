package com.example.onenthapp

import ImageSliderAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.with
import androidx.compose.ui.semantics.error
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.onenthapp.data.GroupPurchaseDetailResult
import com.example.onenthapp.data.PlusRepository
import com.example.onenthapp.databinding.FragmentProductDetailBinding
import kotlinx.coroutines.launch

class GroupPurchaseDetailFragment : Fragment() {
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val repo = PlusRepository()
    private lateinit var imageSliderAdapter: ImageSliderAdapter

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
            tvPrice.text         = "${d?.price}원"
            tvCategory.text      = categoryLabel
            tvProductDue.text    = d?.expirationDate ?: "없음"
            tvMethod.text        = if(d?.purchaseMethod == "OFFLINE") "직거래" else "온라인"
            tvSellerName.text    = d?.writerNickname
            tvStatus.text        = d?.statusLabel

            // 1. 프로필 이미지 로딩 (ivSellerProfile ID 사용 가정)
            val profileUrl = d?.writerProfileImageUrl
            if (!profileUrl.isNullOrBlank()) {
                Glide.with(this@GroupPurchaseDetailFragment) // Fragment 컨텍스트 사용
                    .load(profileUrl)
                    .circleCrop()
                    .placeholder(R.drawable.profile_base) // res/drawable/profile_base.xml 필요
                    .error(R.drawable.profile_base)
                    .into(sellerProfile) // binding.ivSellerProfile
            } else {
                sellerProfile.setImageResource(R.drawable.profile_base)
            }

            // 2. ViewPager2 상품 이미지 슬라이더 (viewpagerProductImages, dotsIndicatorProduct ID 사용 가정)
            // d.imageUrls 가 List<String> 형태의 상품 이미지 URL 리스트라고 가정
            if (!d?.imageUrls.isNullOrEmpty()) {
                viewpagerImages.visibility = View.VISIBLE
                dotsIndicator.visibility = View.VISIBLE

                imageSliderAdapter = ImageSliderAdapter(d.imageUrls)
                viewpagerImages.adapter = imageSliderAdapter
                dotsIndicator.attachTo(viewpagerImages)
            } else {
                // 이미지가 없을 경우 ViewPager와 Indicator를 숨김
                viewpagerImages.visibility = View.GONE
                dotsIndicator.visibility = View.GONE
                // 또는 기본 이미지 하나를 표시할 수도 있음
                // binding.ivProductDefaultImage.visibility = View.VISIBLE (별도의 ImageView 필요)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
