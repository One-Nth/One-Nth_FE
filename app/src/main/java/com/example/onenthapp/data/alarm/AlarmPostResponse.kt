package com.example.onenthapp.data.alarm

data class AlarmPostResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<PostAlarm>
)

data class PostAlarm(
    val alertType: String, // 예: "LIFE_TIP", "DISCOUNT", "RESTAURANT"
    val contentId: Int,
    val message: String,
    val readStatus: Boolean
)
