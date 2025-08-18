package com.example.onenthapp.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.onenthapp.data.login.AuthRepository

class SignupViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignupViewModel::class.java)) {
            return SignupViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
