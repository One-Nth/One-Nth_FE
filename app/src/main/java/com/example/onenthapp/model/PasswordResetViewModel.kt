package com.example.onenthapp.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onenthapp.data.AuthRepository
import kotlinx.coroutines.launch

class PasswordResetViewModel(private val repo: AuthRepository) : ViewModel() {

    val emailStatus = MutableLiveData<String?>()
    val codeStatus = MutableLiveData<String?>()
    val resetStatus = MutableLiveData<String?>()

    fun requestCode(name: String, email: String) = viewModelScope.launch {
        try {
            val response = repo.requestPasswordResetCode(name, email)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess == true) {
                    emailStatus.postValue("인증번호가 발송되었습니다.")
                } else {
                    emailStatus.postValue(body?.message ?: "요청 실패")
                }
            } else {
                emailStatus.postValue("API 요청 실패 (${response.code()})")
            }
        } catch (e: Exception) {
            emailStatus.postValue("네트워크 오류: ${e.message}")
        }
    }

    fun verifyCode(email: String, code: String) = viewModelScope.launch {
        try {
            val response = repo.verifyPasswordResetCode(email, code)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess == true) {
                    codeStatus.postValue("인증 완료되었습니다.")
                } else {
                    codeStatus.postValue(body?.message ?: "인증번호가 올바르지 않습니다.")
                }
            } else {
                codeStatus.postValue("API 요청 실패 (${response.code()})")
            }
        } catch (e: Exception) {
            codeStatus.postValue("네트워크 오류: ${e.message}")
        }

    }

    fun resetPassword(email: String, newPassword: String) = viewModelScope.launch {
        try {
            val response = repo.resetPassword(email, newPassword)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess == true) {
                    resetStatus.postValue("비밀번호가 재설정되었습니다.")
                } else {
                    resetStatus.postValue(body?.message ?: "비밀번호 재설정 실패")
                }
            } else {
                resetStatus.postValue("서버 오류: ${response.code()}")
            }
        } catch (e: Exception) {
            resetStatus.postValue("네트워크 오류: ${e.message}")
        }
    }
}


