package com.example.onenthapp.data.item

import com.google.gson.annotations.SerializedName

data class BuyResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: BuyResult
)
data class BuyResult(
    @SerializedName("purchaseItem")
    val purchaseItemId: Long
)
data class GroupPurchaseDetailResult(
    val id: Long,
    val title: String,
    val imageUrls: List<String>,
    val purchaseUrl : String,
    val expirationDate: String?,
    val writerNickname: String,
    val writerProfileImageUrl: String,
    val writerVerified: Boolean,
    val itemCategory: String,
    val purchaseMethod: String,
    val price: Int,
    val latitude : Double,
    val longitude : Double,
    val status : String,
    val statusLabel: String
)
