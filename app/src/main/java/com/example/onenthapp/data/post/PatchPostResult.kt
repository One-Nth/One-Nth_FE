package com.example.onenthapp.data.post

data class PatchPostResult(val postId: Long, val createdAt: String?)
data class BaseResponse<T>(val isSuccess: Boolean, val code: String?, val message: String?, val result: T?)
