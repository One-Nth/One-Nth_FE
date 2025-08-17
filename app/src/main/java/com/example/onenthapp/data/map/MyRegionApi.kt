package com.example.onenthapp.data.map
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MyRegionApi {

    // 사용자가 등록한 지역 리스트 조회
    @GET("user-settings/regions")
    suspend fun getMyRegions(): GenericResponse<MyRegionResult>

    // 특정 지역을 등록
    @POST("user-settings/regions")
    suspend fun addMyRegion(
        @Body body: AddRegionRequest
    ): GenericResponse<MyRegion>

    // 특정 지역 삭제
    @DELETE("user-settings/regions/{regionId}")
    suspend fun deleteMyRegion(
        @Path("regionId") regionId: Long
    ): DeleteResponse

    // 특정 지역 메인 설정 - 최대 1개
    @PATCH("user-settings/regions/{regionId}")
    suspend fun setMainRegion(
        @Path("regionId") regionId: Long
    ): GenericResponse<MyRegion>

    // 키워드로 주소 검색
    @GET("user-settings/regions/search")
    suspend fun searchRegions(
        @Query("keyword") keyword: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): GenericResponse<SearchRegionsResult>

    // 지역명 기반으로 해당 지역의 대표 좌표 조회
    @GET("map/regions/center")
    suspend fun getRegionCenter(
        @Query("regionName") regionName: String
    ): GenericResponse<RegionCenterResult>

    // 지역 인증
    @POST("user-settings/regions/{regionId}/auth")
    suspend fun authenticateRegion(
        @Path("regionId") regionId: Long,
        @Body request: RegionAuthRequest
    ): RegionAuthResponse

}