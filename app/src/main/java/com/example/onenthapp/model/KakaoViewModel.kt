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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

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

//    fun loginWithKakao(code: String, callback: (Boolean, String?, Boolean) -> Unit) {
//        val client = OkHttpClient()
//        val json = JSONObject().apply { put("code", code) }
//        val body = json.toString().toRequestBody("application/json".toMediaType())
//
//        val request = Request.Builder()
//            .url("http://10.0.2.2:8080/api/auth/kakao/login")
//            .post(body)
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                callback(false, e.message, false)
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                val responseString = response.body?.string()
//                val json = JSONObject(responseString)
//                val isSuccess = json.optBoolean("isSuccess")
//                val isNew = json.optJSONObject("result")?.optBoolean("isNew") ?: false
//                callback(isSuccess, null, isNew)
//            }
//        })
//    }


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

