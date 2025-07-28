package com.example.onenthapp
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.onenthapp.databinding.FragmentProductDetailBinding

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageSliderAdapter: ImageSliderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPagerWithDummyImages()

         val dotsIndicator = binding.dotsIndicator // XML의 DotsIndicator ID
         dotsIndicator.attachTo(binding.viewpagerImages)
    }

    private fun setupViewPagerWithDummyImages() {
        // 더미 이미지 리소스 ID 리스트
        val dummyImages = listOf(
            R.drawable.image_tissue_1, // 실제 drawable 리소스 이름으로 변경
            R.drawable.image_tissue_2,
            R.drawable.image_tissue_3
            // 필요에 따라 더 많은 이미지 추가
        )

//        if (dummyImages.isEmpty()) {
//            // 이미지가 없을 경우의 처리 (예: ViewPager 숨기기 또는 플레이스홀더 표시)
//            binding.viewpagerImages.visibility = View.GONE
//            binding.dotsIndicator.visibility = View.GONE // 인디케이터도 숨김
//            return
//        }

        imageSliderAdapter = ImageSliderAdapter(dummyImages)
        binding.viewpagerImages.adapter = imageSliderAdapter

        // ViewPager2에 indicator 연결
        binding.dotsIndicator.attachTo(binding.viewpagerImages)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        // ViewPager2의 어댑터를 null로 설정하여 메모리 누수 방지 고려
        // binding.viewpagerImages.adapter = null
        _binding = null
    }
}