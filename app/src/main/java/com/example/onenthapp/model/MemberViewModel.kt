package com.example.onenthapp.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onenthapp.RetrofitInstance
import kotlinx.coroutines.launch

class MemberViewModel : ViewModel() {
    private val _withdrawStatus = MutableLiveData<String>()
    val withdrawStatus: LiveData<String> = _withdrawStatus

    fun withdraw() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.memberApi.withdraw()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _withdrawStatus.value = "탈퇴 성공"
                } else {
                    _withdrawStatus.value = "탈퇴 실패: ${response.body()?.message ?: response.message()}"
                }
            } catch (e: Exception) {
                _withdrawStatus.value = "오류 발생: ${e.message}"
            }
        }
    }
}
