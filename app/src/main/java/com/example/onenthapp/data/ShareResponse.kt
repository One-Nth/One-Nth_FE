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
