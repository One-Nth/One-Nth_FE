package com.example.onenthapp.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ItemSearchApi {
    // 상품명 검색
    @GET("group-purchases/title")
    suspend fun searchGroupPurchasesByTitle(
        @Query("keyword") keyword: String
    ): Response<ItemSearchApiResponse>

    @GET("sharing-items/title")
    suspend fun searchSharingItemsByTitle(
        @Query("keyword") keyword: String
    ): Response<ItemSearchApiResponse>

    // 지역명, 카테고리명, 태그 검색
    @GET("group-purchases")
    suspend fun searchGroupPurchases(
        @Query("keyword") keyword: String
    ): Response<ItemSearchApiResponse>

    @GET("sharing-items")
    suspend fun searchSharingItems(
        @Query("keyword") keyword: String
    ): Response<ItemSearchApiResponse>
}
