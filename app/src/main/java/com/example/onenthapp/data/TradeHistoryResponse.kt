package com.example.onenthapp.data

data class TradeHistoryResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: TradeHistory
)

data class TradeHistory(
    val userId: Long,
    val reviewCount: Int,
    val totalRating: Float,
    val totalDealsCount: Int
)

// 판매자 프로필 전용 응답
data class SellerProfileResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: SellerProfileResult
)

data class SellerProfileResult(
    val nickname: String,
    val totalSalesCount: Int,
    val totalReviewCount: Int,
    val averageRating: Float,
    val profileImageUrl: String?,
    val mainRegionName: String,
    val items: List<SellerItem>,
    val recentReviews: List<Review>,
    val verified: Boolean
)

data class SellerItem(
    val id: Long,
    val name: String,
    val status: String, // "DEFAULT", "IN_PROGRESS", "COMPLETED"
    val price: Int,
    val itemCategory: String, // "ELECTRONICS", "HOUSEHOLD", "FOOD", "CLOTHING", "MISC"
    val purchaseMethod: String, // "ONLINE", "OFFLINE"
    val thumbnailUrl: String
)