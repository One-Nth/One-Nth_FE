package com.example.onenthapp.data.transaction

data class DealConfirmationResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<ConfirmationList>?
)
data class ConfirmationList(
    val dealConfirmationid: Int,
    val itemId: Int,
    val itemType: String,
    val itemName: String,
    val itemImageUrl: String
)
