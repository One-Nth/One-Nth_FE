package com.example.onenthapp.data.userset

data class UserSettingsResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: UserSettings
)
