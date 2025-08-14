package com.example.onenthapp.data.item

data class ItemSearchApiResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<ItemSearch>
)

data class ItemSearch(
    val id: Long,
    val category: String,
    val title: String,
    val price: String?,
    val thumbnailUrl: String?,
    val bookmarked: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val status: String,
    val statusLabel: String?,
    val purchaseMethod: String?,
    val imageUrls: List<String>?
)
