package com.example.onenthapp.data.map

data class MapMarker(
    val markerType: String,
    val id: Long,
    val title: String,
    val latitude: Double,
    val longitude: Double
)

data class GroupedMarker(
    val latitude: Double,
    val longitude: Double,
    val markers: List<MapMarker>
)

data class MapMarkersResult(
    val groupedMarkers: List<GroupedMarker>
)

data class ItemMarkerDetail(
    val status: String,
    val itemCategory: String,
    val purchaseMethod: String,
    val imageUrls: List<String>,
    val title: String,
    val price: Int?,
    val latitude: Double,
    val longitude: Double,
    val scraped: Boolean
)

data class ItemMarkerDetailResult(
    val itemMarkerDetails: List<ItemMarkerDetail>
)

data class MapItemPreview(
    val id: Long,
    val title: String,
    val price: Int?,
    val imageUrls: List<String>,
    val itemCategory: String,     // ex) "HOUSEHOLD"
    val purchaseMethod: String,   // ex) "OFFLINE"
    val status: String,           // ex) "IN_PROGRESS" / "SOLD_OUT"
    val scraped: Boolean
)