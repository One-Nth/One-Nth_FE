package com.example.onenthapp.data.map

import com.google.gson.annotations.SerializedName

data class GenericResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: T?
)

data class DeleteResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String
)

data class MyRegionResult(
    @SerializedName("myRegions")
    val regions: List<MyRegion>
)


data class MyRegion(
    val regionId: Long,
    val regionName: String,
    val main: Boolean = false
)

data class AddRegionRequest(
    val regionId: Long
)

//검색 결과
data class SimpleRegion(
    val regionId: Long,
    val regionName: String
)

data class Pagination(
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalElements: Long,
    val last: Boolean
)

data class SearchRegionsResult(
    val regions: List<SimpleRegion>,
    val pagination: Pagination
)

// 지역 중심 좌표 결과
data class RegionCenterResult(
    val regionId: Long,
    val regionName: String,
    val latitude: Double,
    val longitude: Double
)