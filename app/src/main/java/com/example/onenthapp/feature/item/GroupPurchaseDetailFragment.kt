package com.example.onenthapp.feature.item

import android.content.Intent
import com.example.onenthapp.feature.item.ImageSliderAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.onenthapp.PurchaseSellerProfileActivity
import com.example.onenthapp.R
import com.example.onenthapp.data.item.BookmarkRepository
import com.example.onenthapp.data.item.GroupPurchaseDetailResult
import com.example.onenthapp.data.item.PlusRepository
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
import kotlinx.coroutines.launch

class GroupPurchaseDetailFragment : Fragment() {
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val repo = PlusRepository()
    private lateinit var imageSliderAdapter: ImageSliderAdapter
    private var mapView: MapView? = null
    private var kakaoMapInstance: KakaoMap? = null
    private var targetLatLng: LatLng? = null
    private var bookmarkRepo = BookmarkRepository()

    private var lastDetail: GroupPurchaseDetailResult? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentProductDetailBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productId = requireArguments().getLong("productId")
        var currentScraped = requireArguments().getBoolean("initialScraped")

        // ✅ 프로필 영역/화살표/닉네임 클릭 → PurchaseSellerProfileActivity
        val goSeller = View.OnClickListener { openPurchaseSellerProfile(lastDetail, productId) }
        binding.layoutSellerInfo.setOnClickListener(goSeller)
        binding.profileArrow.setOnClickListener(goSeller)
        binding.sellerProfile.setOnClickListener(goSeller)
        binding.tvSellerName.setOnClickListener(goSeller)

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
                val ok = if (before) bookmarkRepo.removePurchase(productId) else bookmarkRepo.addPurchase(productId)
                if (!ok) { currentScraped = before; renderIcon(); Toast.makeText(requireContext(), "북마크 실패", Toast.LENGTH_SHORT).show() }
                // 성공 시 상세 API 재조회가 필요하면 여기서 호출해 최신 상태로 동기화
            }
        }
        lifecycleScope.launch {
            try {
                val resp = repo.fetchGroupPurchaseDetail(productId)
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

    private fun bindDetail(d: GroupPurchaseDetailResult?) {
        lastDetail = d
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
            tvPrice.text         = d?.price?.let { "${it}원" } ?: "가격 협의"
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
        val offline = d?.purchaseMethod == "OFFLINE"
        //val hasCoord = d?.latitude != null

        if (offline) {
            binding.offlinePlace.visibility = View.VISIBLE
            //targetLatLng = LatLng.from(d.latitude, d.longitude)
            startMap(d.latitude, d.longitude) // ↓ 아래 함수
        } else {
            // 온라인이면 지도 전체 숨김
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

    // ✅ 프로필 화면으로 이동 (같이사요 전용)
    private fun openPurchaseSellerProfile(d: GroupPurchaseDetailResult?, originProductId: Long) {
        val intent = Intent(requireContext(), PurchaseSellerProfileActivity::class.java).apply {
            putExtra("originProductId", originProductId)

            d?.let {
                putExtra("sellerName", it.writerNickname ?: "")
                putExtra("sellerProfileImageUrl", it.writerProfileImageUrl ?: "")
                putExtra("sellerVerified", it.writerVerified == true)
                putExtra("purchaseMethod", it.purchaseMethod ?: "")
                putExtra("statusLabel", it.statusLabel ?: "")
                putExtra("itemCategory", it.itemCategory ?: "")
                putExtra("price", it.price ?: 0)
                putExtra("expirationDate", it.expirationDate ?: "")
                putExtra("latitude", it.latitude ?: 0.0)
                putExtra("longitude", it.longitude ?: 0.0)
                // 판매자 식별자 필드가 있다면 함께:
                // putExtra("sellerId", it.writerMemberId ?: -1L)
            }
        }
        startActivity(intent)
    }
}