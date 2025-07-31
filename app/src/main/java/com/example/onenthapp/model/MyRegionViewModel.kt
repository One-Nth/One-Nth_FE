package com.example.onenthapp.ui.region

import androidx.compose.foundation.layout.add
import androidx.compose.ui.geometry.isEmpty
import androidx.lifecycle.*
import com.example.onenthapp.data.MyRegion
import com.example.onenthapp.data.MyRegionRepository
import com.example.onenthapp.model.Region
import kotlinx.coroutines.launch

class MyRegionViewModel : ViewModel() {

    private val repo = MyRegionRepository()
    //private val regionRepo = RegionRepository()  // 검색용 (기존 더미 or 실제 API)

    // 1) 검색용 더미 Region 목록
    private val allRegions = listOf(
        Region(1, "서울특별시 상월곡동", null, 37.617014, 127.074047),
        Region(2, "서울특별시 사당동", null, 37.476687, 126.981737),
        Region(3, "서울특별시 마포구 홍대입구", null, 37.557670, 126.924537),
        Region(4, "서울특별시 강남구 역삼동", null, 37.612354, 126.452342),
        // 필요시 추가…
    )

    private val _searchResults = MutableLiveData<List<Region>>(emptyList())
    val searchResults: LiveData<List<Region>> = _searchResults

    private val _myRegions = MutableLiveData<List<MyRegion>>(emptyList())
    val myRegions: LiveData<List<MyRegion>> = _myRegions

    init {
        viewModelScope.launch {
            loadMyRegions()
        }
    }

    private suspend fun loadMyRegions() {
        try {
            _myRegions.postValue(repo.getMyRegions())
        } catch (_: Exception) {
            // TODO: 에러 처리
        }
    }
//
//    /** 검색 (최대 3개) */
//    fun search(query: String) {
//        viewModelScope.launch {
//            if (query.isBlank()) {
//                _searchResults.postValue(emptyList())
//            } else {
//                val results = regionRepo.searchRegions(query).take(3)
//                _searchResults.postValue(results)
//            }
//        }
//    }
    /** 검색: query 포함하는 Region 최대 3개를 _searchResults 에 post */
    fun search(query: String) {
        val results = if (query.isBlank()) {
            emptyList()
        } else {
            allRegions
                .filter { it.name.contains(query) }
                .take(3)
        }
        _searchResults.value = results
    }

    /** 추가: 클릭된 Region → MyRegion 으로 변환 후 _myRegions 에 추가 */
    fun addRegion(region: Region) {
        val current = _myRegions.value ?: emptyList()
        // 중복 방지, 최대 3개
        if (current.size < 3 && current.none { it.regionId == region.id }) {
            val newMyRegion = MyRegion(
                regionId = region.id,
                regionName = region.name,
                // 만약 현재 리스트가 비어있다면, 첫 번째로 추가되는 지역을 main으로 설정
                main = current.isEmpty()
            )
            // 만약 첫 번째가 아닌데 main으로 만들고 싶다면, 기존 main을 false로 바꿔야 함.
            // 여기서는 첫 번째만 main으로 설정하는 간단한 예시.
            // 필요하다면, 새로운 지역을 main으로 하고 나머지를 false로 만드는 로직 추가.

            val newList = current.toMutableList().apply { add(newMyRegion) }
            _myRegions.value = newList
        }
    }

    /** 삭제: X 버튼 누른 MyRegion 을 _myRegions 에서 제거 */
    fun deleteRegion(myRegion: MyRegion) {
        viewModelScope.launch {
            try {
                val updated = repo.deleteMyRegion(myRegion.regionId)
                _myRegions.postValue(updated)
            } catch (_: Exception) {
                // TODO: 실패 토스트
            }
        }
    }

    /** 메인 변경: 터치된 MyRegion 을 main=true, 나머지는 false 로 설정 */
    fun setMainRegion(myRegion: MyRegion) {
        viewModelScope.launch {
            try {
                val updated = repo.setMainRegion(myRegion.regionId)
                _myRegions.postValue(updated)
            } catch (_: Exception) {
                // TODO: 실패 토스트
            }
        }
    }
    }