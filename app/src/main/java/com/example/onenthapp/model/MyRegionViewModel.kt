package com.example.onenthapp.model

import androidx.lifecycle.*
import com.example.onenthapp.data.map.MyRegion
import com.example.onenthapp.data.map.MyRegionRepository
import com.example.onenthapp.data.map.SimpleRegion
import kotlinx.coroutines.launch

class MyRegionViewModel : ViewModel() {

    val repo = MyRegionRepository()
    private val _myRegions = MutableLiveData<List<MyRegion>>(emptyList())
    val myRegions: LiveData<List<MyRegion>> = _myRegions
    // 상단 라벨 노출/문구용
    val mainRegion: LiveData<MyRegion?> = myRegions.map { list ->
        list.firstOrNull { it.main }
    }
    // 검색 제안 + 페이징 상태
    private val _suggestions = MutableLiveData<List<SimpleRegion>>(emptyList())
    val suggestions: LiveData<List<SimpleRegion>> = _suggestions
    private var keyword: String = ""
    private var page: Int = 0
    private var last: Boolean = true
    private val size: Int = 10  // 오버레이 스크롤에 맞게 10개씩
    private fun orderMainFirst(list: List<MyRegion>) =
        list.sortedWith(compareByDescending<MyRegion> { it.main }.thenBy { it.regionName })

    fun loadMyRegions() = viewModelScope.launch {
        runCatching { repo.getMyRegions() }
            .onSuccess { _myRegions.value = orderMainFirst(it) }
            .onFailure { _myRegions.value = emptyList() }
    }

    fun startSearch(newKeyword: String) = viewModelScope.launch {
        keyword = newKeyword.trim()
        if (keyword.isEmpty()) {
            _suggestions.value = emptyList(); return@launch
        }
        page = 0
        val (list, pg) = repo.searchRegions(keyword, page, size)
        last = pg?.last ?: true
        _suggestions.value = list
    }

    fun loadMore() = viewModelScope.launch {
        if (keyword.isEmpty() || last) return@launch
        val next = page + 1
        val (list, pg) = repo.searchRegions(keyword, next, size)
        last = pg?.last ?: true
        if (list.isNotEmpty()) {
            page = next
            _suggestions.value = (_suggestions.value ?: emptyList()) + list
        }
    }

    fun add(regionId: Long) = viewModelScope.launch {
        runCatching { repo.addMyRegion(regionId) }
            .onSuccess { _myRegions.value = orderMainFirst(it); _suggestions.value = emptyList() }
    }

    fun delete(mr: MyRegion) = viewModelScope.launch {
        if (mr.main) return@launch // 메인 삭제 방지 UX(서버 정책 일치 시)
        runCatching { repo.deleteMyRegion(mr.regionId) }
            .onSuccess { _myRegions.value = orderMainFirst(it) }
    }

    fun setMain(mr: MyRegion) = viewModelScope.launch {
        val current = _myRegions.value.orEmpty()
        val optimistic = mutableListOf<MyRegion>().apply {
            add(mr.copy(main = true))
            addAll(current.filter { it.regionId != mr.regionId }.map { it.copy(main = false) })
        }
        _myRegions.value = optimistic

        runCatching { repo.setMainRegion(mr.regionId) }
            .onSuccess { _myRegions.value = orderMainFirst(it) }
            .onFailure { _myRegions.value = current }
    }
}