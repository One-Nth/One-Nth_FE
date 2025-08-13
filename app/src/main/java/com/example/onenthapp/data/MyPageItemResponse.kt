package com.example.onenthapp.data

data class MyPageItemsResponse(
    val isSuccess: Boolean,
    val code: String?,
    val message: String?,
    val result: MyPageItemsResult?
)

data class MyPageItemsResult(
    val items: List<MyPostProductItem>,
    val page: Int,
    val size: Int,
    val totalCount: Int,
    val hasNext: Boolean
)

data class MyPostProductItem(
    val itemId: Long,
    val itemType: String,      // "PURCHASE" | "SHARE"
    val productName: String,
    val price: Long,
    val quantity: Int,
    val originalPrice: Long,
    val createdTime: String,    // e.g. "2025-08-10T21:07:28.303820"
    val imageUrl: String?
)
