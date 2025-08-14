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
}
