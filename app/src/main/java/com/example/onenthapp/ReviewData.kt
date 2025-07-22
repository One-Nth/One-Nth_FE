package com.example.onenthapp

data class ReviewData(
    val name: String,       // 유저 닉네임
    val rating: Int,        // 별점 (1~5)
    val text: String,       // 후기 내용
)
