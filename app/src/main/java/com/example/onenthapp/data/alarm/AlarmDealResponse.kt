package com.example.onenthapp.data.alarm

data class AlarmDealResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<DealAlarm>
)

data class DealAlarm(
    val alertType: String, // 예: "ITEM", "REVIEW"
    val itemType: String,  // 예: "PURCHASE", "SHARING"
    val contentId: Int,
    val message: String,
    val readStatus: Boolean
)