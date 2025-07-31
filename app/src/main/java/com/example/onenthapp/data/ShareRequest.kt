package com.example.onenthapp.data

class ShareRequest (
    val title: String,
    val quantity: Int,
    val price: Int,
    val itemCategory: String,
    val expirationDate: String,
    val isAvailable: Boolean,
    val purchaseMethod: String,
    val regionId: Long,
    val tags: List<String>
)