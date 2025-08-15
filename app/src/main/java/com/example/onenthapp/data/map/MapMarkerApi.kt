package com.example.onenthapp.data.map

import retrofit2.http.GET
import retrofit2.http.Query

interface MapMarkerApi {
    // /api/map/markers/items?markerType=purchase-item&regionId=1
    @GET("map/markers/items")
    suspend fun getItemMarkers(
        @Query("markerType") markerType: String,
        @Query("regionId") regionId: Long? = null // 선택적
    ): GenericResponse<MapMarkersResult>

    @GET("map/markers/items/details")
    suspend fun getMarkerItemDetails(
        @Query("markerType") markerType: String,         // "PURCHASEITEM"/"SHARINGITEM"
        @Query("itemIds") itemIds: List<Long>            // ?itemIds=1&itemIds=3...
    ): GenericResponse<ItemMarkerDetailResult>

    // 할인정보 게시판과 맛집 게시판용 마커 API
    @GET("map/markers/posts")
    suspend fun getPostMarkers(
        @Query("markerType") markerType: String,         // "DISCOUNT"/"RESTAURANT"
        @Query("regionId") regionId: Long? = null        // 선택적
    ): GenericResponse<PostMarkersResult>

    @GET("map/markers/posts/details")
    suspend fun getPostMarkerDetails(
        @Query("markerType") markerType: String,         // "DISCOUNT"/"RESTAURANT"
        @Query("postIds") postIds: List<Long>            // ?postIds=1&postIds=3...
    ): GenericResponse<PostMarkerDetailResult>

}
