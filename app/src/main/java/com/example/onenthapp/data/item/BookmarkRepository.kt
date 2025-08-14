package com.example.onenthapp.data.item

import com.example.onenthapp.RetrofitInstance

class BookmarkRepository(
    private val api : BookmarkApi = RetrofitInstance.bookmarkApi
) {
    suspend fun addPurchase(id: Long): Boolean =
        runCatching { api.addPurchase(id) }.getOrNull()?.isSuccess == true

    suspend fun addSharing(id: Long): Boolean =
        runCatching { api.addSharing(id) }.getOrNull()?.isSuccess == true

    suspend fun removePurchase(id: Long): Boolean =
        runCatching { api.removePurchase(id) }.getOrNull()?.isSuccess == true

    suspend fun removeSharing(id: Long): Boolean =
        runCatching { api.removeSharing(id) }.getOrNull()?.isSuccess == true

}