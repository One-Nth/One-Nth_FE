package com.example.onenthapp.data.item

import com.example.onenthapp.RetrofitInstance
import retrofit2.Response

class ItemSearchRepository {
    private val api = RetrofitInstance.itemSearchApi

    // 상품명 검색
    suspend fun searchGroupPurchasesByTitle(keyword: String): Response<ItemSearchApiResponse> =
        api.searchGroupPurchasesByTitle(keyword = keyword)

    suspend fun searchSharingItemsByTitle(keyword: String): Response<ItemSearchApiResponse> =
        api.searchSharingItemsByTitle(keyword = keyword)

    // 지역명, 카테고리명, 태그 검색
    suspend fun searchGroupPurchases(keyword: String): Response<ItemSearchApiResponse> =
        api.searchGroupPurchases(keyword = keyword)

    suspend fun searchSharingItems(keyword: String): Response<ItemSearchApiResponse> =
        api.searchSharingItems(keyword = keyword)
}
