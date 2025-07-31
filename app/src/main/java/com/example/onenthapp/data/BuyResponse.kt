package com.example.onenthapp.data

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