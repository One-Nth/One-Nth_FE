package com.example.onenthapp.data

import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.model.Region
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
            resp.result.regions
        } else throw Exception("지역 등록 실패: ${resp.message}")
    }

    suspend fun deleteMyRegion(regionId: Long): List<MyRegion> = withContext(Dispatchers.IO) {
        val resp = api.deleteMyRegion(regionId)
        if (resp.isSuccess && resp.result != null) {
            resp.result.regions
        } else throw Exception("지역 삭제 실패: ${resp.message}")
    }

    suspend fun setMainRegion(regionId: Long): List<MyRegion> = withContext(Dispatchers.IO) {
        val resp = api.setMainRegion(regionId)
        if (resp.isSuccess && resp.result != null) {
            resp.result.regions
        } else throw Exception("메인 지역 변경 실패: ${resp.message}")
    }

    // TODO: 실제 API 연동 전까지는 더미 데이터 활용
    private val allRegions = listOf(
        Region(2, "서울시 상월곡동", "", 37.617014, 127.074047),
        Region(5, "서울시 사당동", "", 37.476687, 126.981737),
        Region(10, "서울시 마포구 홍대입구", "", 37.557670, 126.924537),
        Region(1, "서울시 강남구 역삼동", "", 37.612354, 126.452342),

        // ... 필요시 추가
    )

    fun searchRegions(query: String): List<Region> =
        allRegions.filter { it.name.contains(query) }
}