package com.example.onenthapp.data

import com.google.gson.annotations.SerializedName

data class MyRegionResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: T?
)

data class MyRegionResult(
    @SerializedName("myRegions")
    val regions: List<MyRegion>
)


data class AddRegionRequest(
    val regionId: Long
)

data class MyRegion(
    val regionId: Long,
    val regionName: String,
    val main: Boolean = false
)
