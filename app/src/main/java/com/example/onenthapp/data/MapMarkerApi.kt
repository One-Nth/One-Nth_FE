package com.example.onenthapp.data

import com.example.onenthapp.data.MapMarkersResult
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

}
