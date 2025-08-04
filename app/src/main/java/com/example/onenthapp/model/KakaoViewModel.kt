package com.example.onenthapp.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onenthapp.data.AuthRepository
import com.example.onenthapp.data.KakaoLoginResponse
import com.example.onenthapp.data.KakaoSignupRequest
import kotlinx.coroutines.launch

class KakaoViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _loginResult = MutableLiveData<KakaoLoginResponse>()
    val loginResult: LiveData<KakaoLoginResponse> get() = _loginResult

    fun loginWithKakao(code: String, callback: (Boolean, String?, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.loginWithKakao(code)
                callback(true, null, response.result.isNew)
            } catch (e: Exception) {
                Log.e("KakaoLogin", "error: ${e.message}")
                callback(false, e.message, false)
            }
        }
    }


    fun signupWithKakao(req: KakaoSignupRequest) {
        viewModelScope.launch {
            try {
                repository.signupWithKakao(req)
                // 이후 로직 필요 시 여기에 추가
            } catch (e: Exception) {
                Log.e("KakaoSignup", "error: ${e.message}")
            }
        }
    }
}

