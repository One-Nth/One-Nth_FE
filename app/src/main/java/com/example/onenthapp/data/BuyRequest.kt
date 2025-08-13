package com.example.onenthapp.data

data class BuyRequest(
    val name: String,
    val purchaseMethod: String,
    val itemCategory: String,
    val purchaseUrl: String?,
    val purchaseLocation: String?,
    val price: Int?,
    val tags: List<String>,
    val expirationDate: String?
)