package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
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
import com.example.onenthapp.util.TokenManager
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class PurchaseSellerProfileActivity : AppCompatActivity() {

    private val toolbar by lazy { findViewById<MaterialToolbar>(R.id.topAppBar) }
    private val ivProfile by lazy { findViewById<ImageView>(R.id.profileImage) }
    private val tvNickname by lazy { findViewById<TextView>(R.id.nickname) }
    private val tvSellCount by lazy { findViewById<TextView>(R.id.sellCount) }
    private val tvReviewCount by lazy { findViewById<TextView>(R.id.reviewCount) }
    private val ratingSummary by lazy { findViewById<RatingBar>(R.id.ratingSummary) }
    private val rvBuyerReviews by lazy { findViewById<RecyclerView>(R.id.buyerReviewRecyclerView) }
    private val rvSaleItems by lazy { findViewById<RecyclerView>(R.id.saleItemsRecyclerView) }
    private val scrollBar by lazy { findViewById<View>(R.id.scrollBar) }

    private val memberApi get() = RetrofitInstance.memberApi
    private val reviewApi get() = RetrofitInstance.reviewApi

    private lateinit var buyerAdapter: BuyerReviewAdapter
    private lateinit var sellerItemAdapter: SellerItemAdapter

    private val regionText by lazy { findViewById<TextView>(R.id.regionText) }

    private val btnBlock by lazy { findViewById<View>(R.id.blockbtn) }
    private var targetMemberId: Long? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_profile)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val sellerId       = intent.getLongExtra("sellerId", -1L).takeIf { it > 0 }
        val sellerName     = intent.getStringExtra("sellerName").orEmpty()
        val sellerImageUrl = intent.getStringExtra("sellerProfileImageUrl").orEmpty()
        targetMemberId = sellerId // 인텐트로 들어오면 먼저 세팅

        supportActionBar?.title = if (sellerName.isNotBlank()) sellerName else "프로필"
        if (sellerName.isNotBlank()) tvNickname.text = sellerName
        if (sellerImageUrl.isNotBlank()) {
            Glide.with(this).load(sellerImageUrl).circleCrop()
                .placeholder(R.drawable.profile_base).error(R.drawable.profile_base)
                .into(ivProfile)
        } else ivProfile.setImageResource(R.drawable.profile_base)

        // 전체보기 -> 상세도 PURCHASE만 보이게
        findViewById<View>(R.id.btnGoAllReviews).setOnClickListener {
            val id = intent.getLongExtra("sellerId", -1L).takeIf { it > 0 }
            if (id != null) {
                startActivity(Intent(this, FilteredBuyerReviewsActivity::class.java).apply {
                    putExtra("userId", id)
                    putExtra("itemTypeFilter", "PURCHASE")
                })
            } else {
                Toast.makeText(this, "판매자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.btnGoAllItems)?.setOnClickListener {
            val id = sellerId ?: return@setOnClickListener
            startActivity(Intent(this, SellerItemDetailActivity::class.java).apply {
                putExtra("sellerId", id)
                putExtra("sellerName", sellerName)
                putExtra("itemType", "group-purchase")
            })
        }

        // 차단하기 버튼
        btnBlock.setOnClickListener {
            val token = TokenManager.getAccessToken()
            if (token.isNullOrEmpty()) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = targetMemberId
            if (id == null) {
                Toast.makeText(this, "판매자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnBlock.isEnabled = false
            lifecycleScope.launch {
                try {
                    val res = RetrofitInstance.messageApi.blockMember(id.toInt()) // Long 시그니처면 그대로 id
                    if (res.isSuccessful && res.body()?.isSuccess == true) {
                        val displayName = (intent.getStringExtra("sellerName").orEmpty()
                            .ifBlank { tvNickname.text?.toString().orEmpty() })
                            .ifBlank { "사용자" }

                        Toast.makeText(
                            this@PurchaseSellerProfileActivity,
                            "${displayName}님을 차단하였습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        // 필요 시 finish()
                    } else {
                        Toast.makeText(
                            this@PurchaseSellerProfileActivity,
                            res.body()?.message ?: "차단에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@PurchaseSellerProfileActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    btnBlock.isEnabled = true
                }
            }
        }
        buyerAdapter = BuyerReviewAdapter()
        rvBuyerReviews.adapter = buyerAdapter
        rvBuyerReviews.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        attachScrollBar(rvBuyerReviews, scrollBar)

        sellerItemAdapter = SellerItemAdapter()
        rvSaleItems.adapter = sellerItemAdapter
        rvSaleItems.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvSaleItems.minimumHeight = (220 * resources.displayMetrics.density).toInt()

        sellerId?.let {
            loadGroupPurchaseSummary(it)            // 요약/아이템: purchase 전용 프로필 API
            loadBuyerReviewsFiltered(it, "PURCHASE")// 후기 미리보기: 리뷰 API + PURCHASE만
        } ?: run { fallbackZeros() }
    }

    private fun attachScrollBar(rv: RecyclerView, bar: View) {
        val width = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 350f, resources.displayMetrics
        ).toInt()
        rv.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val offset = recyclerView.computeHorizontalScrollOffset()
                val extent = recyclerView.computeHorizontalScrollExtent()
                val range = recyclerView.computeHorizontalScrollRange()
                val p = offset.toFloat() / (range - extent).coerceAtLeast(1).toFloat()
                bar.translationX = (width - bar.width) * p
            }
        })
    }

    // 요약/아이템: group-purchases 프로필 API 유지
    private fun loadGroupPurchaseSummary(userId: Long) {
        val token = "Bearer ${TokenManager.getAccessToken() ?: ""}"
        lifecycleScope.launch {
            runCatching { memberApi.getGroupPurchaseSellerProfile(token, userId) }
                .onSuccess { resp ->
                    if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                        val r = resp.body()!!.result
                        tvSellCount.text = r.totalSalesCount.toString()
                        tvReviewCount.text = r.totalReviewCount.toString()
                        ratingSummary.setIsIndicator(true)
                        ratingSummary.rating = r.averageRating.coerceIn(0f, 5f)
                        tvNickname.text = r.nickname
                        supportActionBar?.title = r.nickname
                        Glide.with(this@PurchaseSellerProfileActivity)
                            .load(r.profileImageUrl)
                            .circleCrop()
                            .placeholder(R.drawable.profile_base)
                            .error(R.drawable.profile_base)
                            .into(ivProfile)
                        sellerItemAdapter.updateList(r.items)

                        // ✅ 지역 표시: "서울특별시 성북구 상월곡동" -> "상월곡동"
                        val dongOnly = extractDong(r.mainRegionName)
                        regionText.apply {
                            when {
                                r.verified == true && !dongOnly.isNullOrBlank() -> {
                                    text = "$dongOnly 인증 완료"
                                    setTextColor(getColor(R.color.main_green_2))
                                }
                                !dongOnly.isNullOrBlank() -> {
                                    text = "$dongOnly 인증 완료"           // ✅ 동만 노출 (미인증)
                                    setTextColor(getColor(R.color.main_green_2))
                                }
                                else -> {
                                    text = "인증된 지역 없음"
                                    setTextColor(getColor(R.color.main_green_2))
                                }
                            }
                            visibility = View.VISIBLE
                        }


                    } else fallbackZeros()
                }
                .onFailure { fallbackZeros() }
        }
    }

    // 후기 미리보기: 리뷰 API + 타입 필터
    private fun loadBuyerReviewsFiltered(userId: Long, filterType: String) {
        val token = "Bearer ${TokenManager.getAccessToken() ?: ""}"
        lifecycleScope.launch {
            runCatching { reviewApi.getUserReviews(userId, token) }
                .onSuccess { resp ->
                    if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                        val list = resp.body()!!.result.reviewList
                            .filter { it.itemType.equals(filterType, ignoreCase = true) }
                            .map { r ->
                                com.example.onenthapp.data.MyReview(
                                    reviewId = r.reviewId,
                                    itemType = r.itemType,
                                    itemId = r.itemId,
                                    itemTitle = r.itemTitle,
                                    createdAt = r.createdAt,
                                    reviewerId = r.reviewerId,
                                    reviewerNickName = r.reviewerNickName ?: "익명",
                                    reviewerProfileImageUrl = r.reviewerProfileImageUrl,
                                    reviewTargetId = r.reviewTargetId,
                                    content = r.content ?: "",
                                    rate = r.rate,
                                    reviewImageList = r.reviewImageList ?: emptyList()
                                )
                            }
                        buyerAdapter.updateList(list)
                    } else buyerAdapter.updateList(emptyList())
                }
                .onFailure { buyerAdapter.updateList(emptyList()) }
        }
    }

    private fun fallbackZeros() {
        tvSellCount.text = "0"
        tvReviewCount.text = "0"
        ratingSummary.setIsIndicator(true)
        ratingSummary.rating = 0f
        sellerItemAdapter.updateList(emptyList())
        buyerAdapter.updateList(emptyList())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) { finish(); true } else super.onOptionsItemSelected(item)
    }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun extractDong(full: String?): String? {
        if (full.isNullOrBlank()) return null
        val tokens = full.replace(",", " ")
            .replace("·", " ")
            .split(" ")
            .filter { it.isNotBlank() }
        return tokens.asReversed().firstOrNull { it.endsWith("동") || it.endsWith("가") }
    }

}
