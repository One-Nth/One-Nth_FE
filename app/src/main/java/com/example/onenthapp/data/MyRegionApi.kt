package com.example.onenthapp.data
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface MyRegionApi {

    // 사용자가 등록한 지역 리스트 조회
    @GET("user-settings/regions")
    suspend fun getMyRegions(): MyRegionResponse<MyRegionResult>

    // 특정 지역을 등록
    @POST("user-settings/regions")
    suspend fun addMyRegion(
        @Body body: AddRegionRequest
    ): MyRegionResponse<MyRegionResult>

    // 특정 지역 삭제
    @DELETE("user-settings/regions/{regionId}")
    suspend fun deleteMyRegion(
        @Path("regionId") regionId: Long
    ): MyRegionResponse<MyRegionResult>

    // 특정 지역 메인 설정 - 최대 1개
    @PATCH("user-settings/regions/{regionId}/main")
    suspend fun setMainRegion(
        @Path("regionId") regionId: Long
    ): MyRegionResponse<MyRegionResult>

}