package com.example.onenthapp

import ImageSliderAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.onenthapp.data.BookmarkRepository
import com.example.onenthapp.data.SharingItemDetailResult
import com.example.onenthapp.data.PlusRepository
import com.example.onenthapp.databinding.FragmentProductDetailBinding
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import kotlinx.coroutines.launch

class SharingItemDetailFragment : Fragment() {
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val repo = PlusRepository()
    private lateinit var imageSliderAdapter: ImageSliderAdapter
    private var mapView: MapView? = null
    private var kakaoMapInstance: KakaoMap? = null
    private var targetLatLng: LatLng? = null
    private var bookmarkRepo = BookmarkRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentProductDetailBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productId = requireArguments().getLong("productId")
        var currentScraped = requireArguments().getBoolean("initialScraped")
        
        fun renderIcon() {
            binding.btnBookmark.setImageResource(
                if (currentScraped) R.drawable.ic_bookmark_on else R.drawable.ic_bookmark_off
            )
        }
        renderIcon()
        
        binding.btnBookmark.setOnClickListener {
            val before = currentScraped
            currentScraped = !before
            renderIcon()

            viewLifecycleOwner.lifecycleScope.launch {
                val ok = if (before) bookmarkRepo.removeSharing(productId) else bookmarkRepo.addSharing(productId)
                if (!ok) { 
                    currentScraped = before; 
                    renderIcon(); 
                    Toast.makeText(requireContext(), "북마크 실패", Toast.LENGTH_SHORT).show() 
                }
            }
        }
        
        lifecycleScope.launch {
            try {
                val resp = repo.fetchSharingItemDetail(productId)
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    bindDetail(resp.body()!!.result)
                } else {
                    Toast.makeText(requireContext(), "불러오기 실패: ${resp.code()} ${resp.errorBody()?.string() ?: resp.message()} ", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun bindDetail(d: SharingItemDetailResult?) {
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
            tvProductName.text   = d?.title
            // URL 대신 판매 수량 표시
            tvProductLink.text   = "${d?.quantity}개"
            // 라벨도 "판매 수량"으로 변경
            labelProductLink.text = "판매 수량"
            tvPrice.text         = "${d?.price}원"
            tvCategory.text      = categoryLabel
            tvProductDue.text    = d?.expirationDate ?: "없음"
            tvMethod.text        = if(d?.purchaseMethod == "OFFLINE") "직거래" else "온라인"
            tvSellerName.text    = d?.writerNickname
            // status는 API에서 제공하는 statusLabel 사용
            tvStatus.text        = d?.statusLabel ?: "판매중"

            // 인증 상태 표시
            tvVerification.text  = if(d?.writerVerified == true) "본인 인증 완료" else "본인 인증 미완료"

            // 프로필 이미지 로딩
            val profileUrl = d?.writerProfileImageUrl
            if (!profileUrl.isNullOrBlank()) {
                Glide.with(this@SharingItemDetailFragment)
                    .load(profileUrl)
                    .circleCrop()
                    .placeholder(R.drawable.profile_base)
                    .error(R.drawable.profile_base)
                    .into(sellerProfile)
            } else {
                sellerProfile.setImageResource(R.drawable.profile_base)
            }

            // ViewPager2 상품 이미지 슬라이더
            if (!d?.imageUrls.isNullOrEmpty()) {
                viewpagerImages.visibility = View.VISIBLE
                dotsIndicator.visibility = View.VISIBLE

                imageSliderAdapter = ImageSliderAdapter(d.imageUrls)
                viewpagerImages.adapter = imageSliderAdapter
                dotsIndicator.attachTo(viewpagerImages)
            } else {
                viewpagerImages.visibility = View.GONE
                dotsIndicator.visibility = View.GONE
            }
            
            // item_search_result 레이아웃 바인딩 (sharingitem용)
            // 수량과 가격 정보를 sharingitem에 맞게 표시
            // tvPrice는 이미 위에서 설정됨
            // tvUnit은 "개" 단위로 표시
            // TODO: item_search_result 레이아웃의 tvUnit을 찾아서 설정
        }
        
        val offline = d?.purchaseMethod == "OFFLINE"

        if (offline) {
            binding.offlinePlace.visibility = View.VISIBLE
            // 거래 희망 장소에 regionName 표시
            // binding.tvProductPlace.text = d?.regionName ?: "장소 정보 없음"
            // API에서 제공하는 latitude, longitude 사용 (0이 아닌 경우만)
            if (d?.latitude != null && d?.longitude != null && 
                d.latitude != 0.0 && d.longitude != 0.0) {
                startMap(d.latitude, d.longitude)
            } else {
                // 좌표가 없거나 0인 경우 기본값 사용
                startMap(37.5665, 126.9780) // 서울시청 좌표 (임시)
            }
        } else {
            binding.offlinePlace.visibility = View.GONE
        }
    }
    
    private fun startMap(latitude: Double, longitude: Double) {
        val mapView = binding.locationMap
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {}
            override fun onMapError(e: Exception) {}
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                kakaoMapInstance = map
                val pos = LatLng.from(latitude, longitude)
                map.moveCamera(CameraUpdateFactory.newCenterPosition(pos, 18))

                val lm = map.labelManager ?: return
                val styles = lm.addLabelStyles(
                    LabelStyles.from(
                        LabelStyle.from(R.drawable.marker_green_72)
                            .setAnchorPoint(0.5f, 1.0f)
                            .setApplyDpScale(false)
                    )
                )
                val opts = LabelOptions.from(pos).setStyles(styles)
                lm.layer?.addLabel(opts)
            }

            override fun getZoomLevel() = 18
        })
    }

    override fun onResume() { super.onResume(); mapView?.resume() }
    override fun onPause()  { mapView?.pause(); super.onPause() }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}