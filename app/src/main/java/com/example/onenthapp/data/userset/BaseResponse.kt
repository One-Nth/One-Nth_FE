package com.example.onenthapp.data.userset

data class BaseResponse(
    val httpStatus: String?,
    val isSuccess: Boolean,
    val code: String,
    val message: String
)
