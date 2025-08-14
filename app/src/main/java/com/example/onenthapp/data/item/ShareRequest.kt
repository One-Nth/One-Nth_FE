package com.example.onenthapp.data.item

class ShareRequest (
    val purchaseMethod: String,
    val price: Int?,
    val quantity: Int,
    val sharingLocation: String,
    val itemCategory: String,
    val title: String,
    val tags: List<String>,
    val expirationDate: String,
    val isAvailable: Boolean
)