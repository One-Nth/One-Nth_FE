package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.bumptech.glide.Glide
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.util.TokenManager
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class PurchaseSellerProfileActivity : AppCompatActivity() {

    // Toolbar & Header
    private val toolbar by lazy { findViewById<MaterialToolbar>(R.id.topAppBar) }
    private val ivProfile by lazy { findViewById<ImageView>(R.id.profileImage) }
    private val tvNickname by lazy { findViewById<TextView>(R.id.nickname) }
    private val btnBlock by lazy { findViewById<ImageView>(R.id.blockbtn) } // 필요 시 클릭만 붙여 쓰면 됨

    // Summary
    private val tvSellCount by lazy { findViewById<TextView>(R.id.sellCount) }
    private val tvReviewCount by lazy { findViewById<TextView>(R.id.reviewCount) }
    private val ratingSummary by lazy { findViewById<RatingBar>(R.id.ratingSummary) }

    // Buyer reviews (가로리스트 + 진행바)
    private val rvBuyerReviews by lazy { findViewById<RecyclerView>(R.id.buyerReviewRecyclerView) }
    private val rvSaleItems by lazy { findViewById<RecyclerView>(R.id.saleItemsRecyclerView) }
    private val scrollBar by lazy { findViewById<View>(R.id.scrollBar) }

    private val reviewApi get() = RetrofitInstance.reviewApi
    private val memberApi get() = RetrofitInstance.memberApi

    private lateinit var buyerAdapter: BuyerReviewAdapter
    private lateinit var sellerItemAdapter: SellerItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_profile)

        // Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // ====== 인텐트 값 받기 ======
        val originProductId   = intent.getLongExtra("originProductId", -1L) // 필요하면 사용
        val sellerId          = intent.getLongExtra("sellerId", -1L).takeIf { it > 0 }
        val sellerName        = intent.getStringExtra("sellerName").orEmpty()
        val sellerImageUrl    = intent.getStringExtra("sellerProfileImageUrl").orEmpty()
        val sellerVerified    = intent.getBooleanExtra("sellerVerified", false)
        // 아래 값들은 필요 시 UI에 붙여 사용
        // val purchaseMethod  = intent.getStringExtra("purchaseMethod").orEmpty()
        // val statusLabel     = intent.getStringExtra("statusLabel").orEmpty()
        // val itemCategory    = intent.getStringExtra("itemCategory").orEmpty()
        // val price           = intent.getIntExtra("price", 0)
        // val quantity        = intent.getIntExtra("quantity", 0)
        // val expirationDate  = intent.getStringExtra("expirationDate").orEmpty()
        // val latitude        = intent.getDoubleExtra("latitude", 0.0)
        // val longitude       = intent.getDoubleExtra("longitude", 0.0)

        // ▼ 구매자거래후기 전체보기(화살표) 이동
        findViewById<View>(R.id.btnGoAllReviews).setOnClickListener {
            val id = sellerId
            if (id != null) {
                startActivity(Intent(this, BuyerReviewDetailActivity::class.java).apply {
                    putExtra("userId", id)   // ✅ 동일 키
                })
            } else {
                Toast.makeText(this, "판매자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // ▼ 판매 물품 전체보기(화살표) 이동
        findViewById<View>(R.id.btnGoAllItems)?.setOnClickListener {
            val id = sellerId
            val name = sellerName
            if (id != null) {
                startActivity(Intent(this, SellerItemDetailActivity::class.java).apply {
                    putExtra("sellerId", id)
                    putExtra("sellerName", name)
                    putExtra("itemType", "group-purchase") // 같이사요
                })
            } else {
                Toast.makeText(this, "판매자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Log.e("PurchaseSellerProfile", "btnGoAllItems not found in layout")
        }

        // Header bind
        if (sellerName.isNotBlank()) {
            tvNickname.text = sellerName
            supportActionBar?.title = sellerName
        } else {
            supportActionBar?.title = "프로필"
        }

        if (sellerImageUrl.isNotBlank()) {
            Glide.with(this)
                .load(sellerImageUrl)
                .circleCrop()
                .placeholder(R.drawable.profile_base)
                .error(R.drawable.profile_base)
                .into(ivProfile)
        } else {
            ivProfile.setImageResource(R.drawable.profile_base)
        }

        // 차단 버튼 필요 시
        // btnBlock.setOnClickListener { /* TODO: 차단 API 연결 */ }

        // Buyer reviews 설정
        try {
            buyerAdapter = BuyerReviewAdapter()
            rvBuyerReviews.apply {
                adapter = buyerAdapter
                layoutManager = LinearLayoutManager(
                    this@PurchaseSellerProfileActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            }
            attachScrollBar(rvBuyerReviews, scrollBar) // 진행바 연동

            // 판매 물품 리스트(가로) 설정
            sellerItemAdapter = SellerItemAdapter()
            rvSaleItems.apply {
                adapter = sellerItemAdapter
                layoutManager = LinearLayoutManager(
                    this@PurchaseSellerProfileActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                // RecyclerView 최소 높이 설정
                minimumHeight = (220 * resources.displayMetrics.density).toInt() // 220dp
            }
        } catch (e: Exception) {
            Log.e("PurchaseSellerProfile", "Error setting up RecyclerViews: ${e.message}")
            e.printStackTrace()
        }

        // 거래/후기 로딩 (sellerId 있을 때만)
        sellerId?.let {
            loadSellerProfile(it)
            loadBuyerReviews(it)
        } ?: run {
            // sellerId 없으면 요약/리뷰는 기본값
            tvSellCount.text = "0"
            tvReviewCount.text = "0"
            ratingSummary.setIsIndicator(true)
            ratingSummary.rating = 0f
            buyerAdapter.updateList(emptyList())
            sellerItemAdapter.updateList(emptyList())
        }
    }

    private fun attachScrollBar(rv: RecyclerView, bar: View) {
        // 진행바 트랙의 기준 폭(dp 350 == 너가 레이아웃에서 쓴 가로 길이)
        val trackWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 350f, resources.displayMetrics
        ).toInt()

        rv.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val offset = recyclerView.computeHorizontalScrollOffset()
                val extent = recyclerView.computeHorizontalScrollExtent()
                val range = recyclerView.computeHorizontalScrollRange()

                val proportion = offset.toFloat() / (range - extent).coerceAtLeast(1).toFloat()
                val maxX = trackWidth - bar.width
                bar.translationX = maxX * proportion
            }
        })
    }

    private fun loadSellerProfile(targetUserId: Long) {
        val token = TokenManager.getAccessToken()
        lifecycleScope.launch {
            try {
                // 같이사요 API 사용
                val resp = memberApi.getGroupPurchaseSellerProfile("Bearer ${token ?: ""}", targetUserId)
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val r = resp.body()!!.result
                    
                    // 프로필 정보
                    tvSellCount.text = r.totalSalesCount.toString()
                    tvReviewCount.text = r.totalReviewCount.toString()
                    ratingSummary.setIsIndicator(true)
                    ratingSummary.rating = r.averageRating

                    // 판매 물품 리스트 업데이트
                    sellerItemAdapter.updateList(r.items)
                    
                } else {
                    tvSellCount.text = "0"
                    tvReviewCount.text = "0"
                    ratingSummary.setIsIndicator(true)
                    ratingSummary.rating = 0f
                    sellerItemAdapter.updateList(emptyList())
                    Log.e("PurchaseSellerProfile", "GroupPurchaseSellerProfile fail: ${resp.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                tvSellCount.text = "0"
                tvReviewCount.text = "0"
                ratingSummary.setIsIndicator(true)
                ratingSummary.rating = 0f
                sellerItemAdapter.updateList(emptyList())
                Log.e("PurchaseSellerProfile", "GroupPurchaseSellerProfile err: ${e.message}")
            }
        }
    }

    private fun loadBuyerReviews(targetUserId: Long) {
        val token = TokenManager.getAccessToken()
        lifecycleScope.launch {
            try {
                val resp = reviewApi.getUserReviews(targetUserId, "Bearer ${token ?: ""}")
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val list = resp.body()!!.result.reviewList.map { r ->
                        com.example.onenthapp.data.MyReview(
                            reviewId = r.reviewId,
                            itemType = r.itemType,
                            itemId = r.itemId,
                            itemTitle = r.itemTitle,
                            createdAt = r.createdAt,
                            reviewerId = r.reviewerId,
                            reviewerNickName = r.reviewerNickName,
                            reviewerProfileImageUrl = r.reviewerProfileImageUrl,
                            reviewTargetId = r.reviewTargetId,
                            content = r.content,
                            rate = r.rate,
                            reviewImageList = r.reviewImageList
                        )
                    }
                    buyerAdapter.updateList(list)
                } else {
                    buyerAdapter.updateList(emptyList())
                    Log.e("PurchaseSellerProfile", "Reviews fail: ${resp.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                buyerAdapter.updateList(emptyList())
                Log.e("PurchaseSellerProfile", "Reviews err: ${e.message}")
            }
        }
    }


    // 액션바 업 버튼
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) { finish(); true } else super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}
