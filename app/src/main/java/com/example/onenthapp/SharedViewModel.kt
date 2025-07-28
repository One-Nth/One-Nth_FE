package com.example.onenthapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class HomeTabType {
    BUY, // 0번 탭 (같이사요)
    SHARE// 1번 탭 (함께나눠요)
}

class SharedViewModel : ViewModel() {
    private val _currentHomeTab = MutableLiveData<HomeTabType>(HomeTabType.BUY) // 기본값 BUY
    val currentHomeTab: LiveData<HomeTabType> get() = _currentHomeTab

    fun setCurrentHomeTab(tabType: HomeTabType) {
        _currentHomeTab.value = tabType
    }
}