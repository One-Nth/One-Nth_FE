package com.example.onenthapp.data.map

import com.example.onenthapp.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyRegionRepository {

    private val api = RetrofitInstance.myRegionApi

    suspend fun getMyRegions(): List<MyRegion> = withContext(Dispatchers.IO) {
        val resp = api.getMyRegions()
        if (resp.isSuccess && resp.result != null) {
            resp.result.regions
        } else throw Exception("내 동네 조회 실패: ${resp.message}")
    }

    suspend fun addMyRegion(regionId: Long): List<MyRegion> = withContext(Dispatchers.IO) {
        val resp = api.addMyRegion(AddRegionRequest(regionId))
        if (resp.isSuccess && resp.result != null) {
            getMyRegions()
        } else throw Exception("지역 등록 실패: ${resp.message}")
    }

    suspend fun deleteMyRegion(regionId: Long): List<MyRegion> = withContext(Dispatchers.IO) {
        val resp = api.deleteMyRegion(regionId)
        if (resp.isSuccess) {
            getMyRegions()
        } else throw Exception("지역 삭제 실패: ${resp.message}")
    }

    suspend fun setMainRegion(regionId: Long): List<MyRegion> = withContext(Dispatchers.IO) {
        val resp = api.setMainRegion(regionId)
        if (resp.isSuccess && resp.result != null) {
            getMyRegions()
        } else throw Exception("메인 지역 변경 실패: ${resp.message}")
    }

    suspend fun searchRegions(keyword: String, page: Int, size: Int): Pair<List<SimpleRegion>, Pagination?> = withContext(Dispatchers.IO) {

        //allRegions.filter { it.name.contains(query) }
        val resp = api.searchRegions(keyword = keyword, page = page, size = size)
        if (resp.isSuccess && resp.result != null) resp.result.regions to resp.result.pagination
        else emptyList<SimpleRegion>() to null
    }

    // 지역명 기반으로 해당 지역의 대표 좌표 조회
    suspend fun getRegionCenter(regionName: String): RegionCenterResult? = withContext(Dispatchers.IO) {
        val resp = api.getRegionCenter(regionName)
        if (resp.isSuccess && resp.result != null) resp.result
        else null
    }

    // 지역 인증
    suspend fun authenticateRegion(regionId: Long, latitude: Double, longitude: Double): RegionAuthResult? = withContext(Dispatchers.IO) {
        val request = RegionAuthRequest(latitude, longitude)
        val resp = api.authenticateRegion(regionId, request)
        if (resp.isSuccess && resp.result != null) resp.result
        else throw Exception("지역 인증 실패: ${resp.message}")
    }
}