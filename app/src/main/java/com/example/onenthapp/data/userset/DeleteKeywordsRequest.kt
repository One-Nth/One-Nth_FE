package com.example.onenthapp.data.userset

data class DeleteKeywordsRequest(
    val productKeywordIdList: List<Int>,
    val regionKeywordIdList: List<Int>
)
