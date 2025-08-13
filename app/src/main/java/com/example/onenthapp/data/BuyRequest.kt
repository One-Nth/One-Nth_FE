package com.example.onenthapp.data

data class BuyRequest(
    val purchaseMethod: String,
    val price: Int?,
    val name: String,
    val itemCategory: String,
    val purchaseUrl: String?,
    val purchaseLocation: String?,
    val tags: List<String>,
    val expirationDate: String?
)