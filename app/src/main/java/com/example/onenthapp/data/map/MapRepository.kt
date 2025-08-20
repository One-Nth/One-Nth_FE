package com.example.onenthapp.data.map
import com.example.onenthapp.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapRepository {
    private val api = RetrofitInstance.mapMarkerApi

    suspend fun fetchItemMarker(markerType: String, regionId: Long?): List<GroupedMarker> =
        withContext(Dispatchers.IO) {
            val r = api.getItemMarkers(markerType, regionId)
            if (r.isSuccess && r.result != null) r.result.groupedMarkers else emptyList()
        }
    suspend fun fetchMarkerItemDetails(
        markerType: String,
        itemIds: List<Long>
    ): List<MapItemPreview> = withContext(Dispatchers.IO) {
        val resp = api.getMarkerItemDetails(markerType, itemIds)
        if (resp.isSuccess && resp.result != null) {
            val dtos = resp.result.itemMarkerDetails
            // 응답에 id가 없으니 요청 순서대로 zip
            dtos.mapIndexedNotNull { idx, d ->
                val id = itemIds.getOrNull(idx) ?: return@mapIndexedNotNull null
                MapItemPreview(
                    id = id,
                    title = d.title,
                    price = d.price,
                    imageUrls = d.imageUrls,
                    itemCategory = d.itemCategory,
                    purchaseMethod = d.purchaseMethod,
                    status = d.status,
                    scraped = d.scraped
                )
            }
        } else emptyList()
    }

    // 할인정보 게시판과 맛집 게시판용 마커 관련 함수들
    suspend fun fetchPostMarkers(markerType: String, regionId: Long?): List<GroupedPostMarker> =
        withContext(Dispatchers.IO) {
            val r = api.getPostMarkers(markerType, regionId)
            if (r.isSuccess && r.result != null) r.result.groupedMarkers else emptyList()
        }

    suspend fun fetchPostMarkerDetails(
        markerType: String,
        postIds: List<Long>
    ): List<PostMarkerPreview> = withContext(Dispatchers.IO) {
        val resp = api.getPostMarkerDetails(markerType, postIds)
        if (resp.isSuccess && resp.result != null) {
            val dtos = resp.result.postMarkerDetails
            // 응답에 id가 없으니 요청 순서대로 zip
            dtos.mapIndexedNotNull { idx, d ->
                val id = postIds.getOrNull(idx) ?: return@mapIndexedNotNull null
                
                // 서버 응답에서 isScraped 필드 사용
                val scraped = d.isScraped
                
                PostMarkerPreview(
                    id = id,
                    placeName = d.placeName,
                    title = d.title,
                    address = d.address,
                    createdAt = d.createdAt,
                    latitude = d.latitude,
                    longitude = d.longitude,
                    scraped = scraped
                )
            }
        } else emptyList()
    }
}
