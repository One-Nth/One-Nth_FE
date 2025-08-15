package com.example.onenthapp.feature.item

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.onenthapp.R
import com.example.onenthapp.SharingSellerProfileActivity
import com.example.onenthapp.RetrofitInstance.messageApi
import com.example.onenthapp.data.item.BookmarkRepository
import com.example.onenthapp.data.item.PlusRepository
import com.example.onenthapp.data.item.SharingItemDetailResult
import com.example.onenthapp.databinding.FragmentProductDetailBinding
import com.example.onenthapp.feature.chat.ChatRoomActivity
import com.example.onenthapp.utils.ShareDialogUtil.showShareDialog
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

class SharingItemDetailFragment : Fragment() {
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val repo = PlusRepository()
    private lateinit var imageSliderAdapter: ImageSliderAdapter
    private var mapView: MapView? = null
    private var kakaoMapInstance: KakaoMap? = null
    private var targetLatLng: LatLng? = null
    private var bookmarkRepo = BookmarkRepository()

    private var lastDetail: SharingItemDetailResult? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentProductDetailBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productId = requireArguments().getLong("productId")
        var currentScraped = requireArguments().getBoolean("initialScraped")

        // ✅ 프로필 영역/화살표/닉네임 클릭 → SharingSellerProfileActivity 이동
        val openSellerClick = View.OnClickListener { openSharingSellerProfile(lastDetail, productId) }
        binding.layoutSellerInfo.setOnClickListener(openSellerClick)
        binding.profileArrow.setOnClickListener(openSellerClick)
        binding.sellerProfile.setOnClickListener(openSellerClick)
        binding.tvSellerName.setOnClickListener(openSellerClick)

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

        // 공유 버튼 클릭 리스너 추가
        binding.btnShare.setOnClickListener {
            val shareUrl = "https://onenthapp.com/sharing/${productId}"
            showShareDialog(shareUrl, "상품을 공유하시겠습니까?")
        }
    }
    private fun bindDetail(d: SharingItemDetailResult?) {
        lastDetail = d
        binding.btnChat.setOnClickListener {
            val targetMemberId = d?.writerid ?: return@setOnClickListener
            val chatRoomType = "SHARING" // 또는 "GROUP_PURCHASE" 등

            lifecycleScope.launch {
                try {
                    val response = messageApi.createChatRoom(targetMemberId.toInt(), chatRoomType)
                    if (response.isSuccessful && response.body() != null) {
                        // roomId는 필요 없다고 하셨으니 생략
                        val intent = Intent(requireContext(), ChatRoomActivity::class.java).apply {
                            putExtra("targetMemberId", targetMemberId)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), "채팅방 생성 실패", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }


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
            tvVerification.text  = if(d?.writerVerified == true) "지역 인증 완료" else "지역 인증 미완료"
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

    // ✅ 판매자 프로필 화면으로 이동 (나눔/공유 전용)
    private fun openSharingSellerProfile(d: SharingItemDetailResult?, originProductId: Long) {
        if (d?.writerid == null) {
            Toast.makeText(requireContext(), "판매자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(requireContext(), SharingSellerProfileActivity::class.java).apply {
            putExtra("originProductId", originProductId)
            putExtra("sellerId", d.writerid)
            putExtra("sellerName", d.writerNickname ?: "")
            putExtra("sellerProfileImageUrl", d.writerProfileImageUrl ?: "")
            putExtra("sellerVerified", d.writerVerified == true)
        }
        startActivity(intent)
    }
}
