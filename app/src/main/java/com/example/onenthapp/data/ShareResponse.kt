package com.example.onenthapp.data

import com.google.gson.annotations.SerializedName

data class ShareResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: ShareResult
)
data class ShareResult(
    @SerializedName("sharingItem")
    val sharingItemId: Long
)

data class SharingItemDetailResult(
    val id: Int,
    val title: String,
    val quantity: Int,
    val price: Int,
    val itemCategory: String,
    val expirationDate: String?,
    val isAvailable: Boolean,
    val purchaseMethod: String,
    val sharingLocation: String?,
    val imageUrls: List<String>,
    val tags: List<String>,
    val writerNickname: String
)