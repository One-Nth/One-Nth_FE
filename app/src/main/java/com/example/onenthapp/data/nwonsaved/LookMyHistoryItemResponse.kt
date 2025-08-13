package com.example.onenthapp.data.nwonsaved

data class LookMyHistoryItemResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<MyHistoryItem>
)

data class MyHistoryItem(
    val itemId: Int,
    val itemType: String,
    val itemName: String,
    val itemImageUrl: String
)
