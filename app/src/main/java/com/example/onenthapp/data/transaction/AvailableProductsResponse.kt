package com.example.onenthapp.data.transaction

data class AvailableProductsResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<AvailableProduct>?
)
